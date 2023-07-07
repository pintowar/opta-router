package io.github.pintowar.opta.router.solver.ortools

import com.google.ortools.Loader
import com.google.ortools.constraintsolver.*
import com.google.protobuf.Duration
import io.github.pintowar.opta.router.core.domain.models.*
import io.github.pintowar.opta.router.core.domain.models.matrix.Matrix
import io.github.pintowar.opta.router.core.solver.SolverConfig
import io.github.pintowar.opta.router.core.solver.spi.Solver
import java.util.*
import java.util.function.LongBinaryOperator
import java.util.function.LongUnaryOperator

class OrToolsSolver(key: UUID, name: String, config: SolverConfig) : Solver(key, name, config) {

    init {
        Loader.loadNativeLibraries()
    }

    private class DistanceEval(
        val matrix: Matrix, val manager: RoutingIndexManager, val idxLocations: Map<Int, Location>, val k: Long = 100
    ) : LongBinaryOperator {
        override fun applyAsLong(p1: Long, p2: Long): Long = try {
            val fromNode = idxLocations.getValue(manager.indexToNode(p1)).id
            val toNode = idxLocations.getValue(manager.indexToNode(p2)).id

            (matrix.distance(fromNode, toNode) * k).toLong()
        } catch (e: Throwable) {
            0L
        }
    }

    private class DemandEval(
        val manager: RoutingIndexManager, val idxLocations: Map<Int, Location>
    ) : LongUnaryOperator {
        override fun applyAsLong(fromIndex: Long): Long {
            val fromNode = manager.indexToNode(fromIndex)
            return when (val loc = idxLocations[fromNode]) {
                is Customer -> loc.demand.toLong()
                else -> 0L
            }
        }
    }

    private class ProblemSummary(problem: VrpProblem) {
        val nVehicles = problem.vehicles.size
        val vehiclesCapacities = problem.vehicles.map { it.capacity.toLong() }.toLongArray()
        val idLocations = problem.locations.associateBy { it.id }
        val idxLocations = problem.locations.withIndex().associate { it.index to it.value }
        val locationsIdx = idxLocations.map { it.value to it.key }.toMap()
        val nLocations = idxLocations.size
        val depots = problem.vehicles.mapNotNull { locationsIdx[it.depot] }.toIntArray()

        fun locationIdxFromCustomer(customerId: Long) = locationsIdx.getValue(idLocations.getValue(customerId)).toLong()
    }

    @Volatile
    private var running = false

    override fun solve(initialSolution: VrpSolution, matrix: Matrix, callback: (VrpSolutionRequest) -> Unit) {
        var best = initialSolution
        val summary = ProblemSummary(initialSolution.problem)

        val manager = RoutingIndexManager(summary.nLocations, summary.nVehicles, summary.depots, summary.depots)
        val model = RoutingModel(manager)

        val transitRegistry = model.registerTransitCallback(DistanceEval(matrix, manager, summary.idxLocations))
        model.setArcCostEvaluatorOfAllVehicles(transitRegistry)

        val demandCallbackIndex: Int = model.registerUnaryTransitCallback(DemandEval(manager, summary.idxLocations))
        model.addDimensionWithVehicleCapacity(demandCallbackIndex, 0, summary.vehiclesCapacities, true, "Capacity")

        val searchParameters = main.defaultRoutingSearchParameters().toBuilder()
            .setFirstSolutionStrategy(FirstSolutionStrategy.Value.AUTOMATIC)
            .setTimeLimit(Duration.newBuilder().setSeconds(config.timeLimit.toSeconds()).build())
            .setLocalSearchMetaheuristic(LocalSearchMetaheuristic.Value.GUIDED_LOCAL_SEARCH)
            .setLogSearch(true)
            .build()

        model.addAtSolutionCallback {
            if (isSolving()) {
                val actual = toDTO(model, manager, initialSolution.problem, summary.idxLocations, matrix)
                if (best.isEmpty() || actual.getTotalDistance() < best.getTotalDistance()) {
                    best = actual
                    callback(VrpSolutionRequest(best, SolverStatus.RUNNING, key))
                }
            } else {
                model.solver().finishCurrentSearch()
            }
        }

        running = true
        val solution = if (!initialSolution.isEmpty()) {
            model.closeModelWithParameters(searchParameters)
            val vehicleVisitOrder = initialSolution.routes.map { route ->
                route.customerIds.map(summary::locationIdxFromCustomer).toLongArray()
            }.toTypedArray()
            val initialState = model.readAssignmentFromRoutes(vehicleVisitOrder, true)
            model.solveFromAssignmentWithParameters(initialState, searchParameters)
        } else {
            model.solveWithParameters(searchParameters)
        }

        try {
            if (solution != null) {
                val sol = toDTO(model, manager, initialSolution.problem, summary.idxLocations, matrix, solution)
                callback(VrpSolutionRequest(sol, SolverStatus.TERMINATED, key))
            } else throw IllegalStateException("Couldn't find an optimal solution")
        } finally {
            running = false
            model.delete()
        }
    }

    override fun terminate() {
        running = false
    }

    override fun isSolving() = running

}