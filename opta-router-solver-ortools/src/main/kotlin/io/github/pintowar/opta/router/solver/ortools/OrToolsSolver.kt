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

    @Volatile
    private var running = false

    override fun solve(initialSolution: VrpSolution, matrix: Matrix, callback: (VrpSolutionRequest) -> Unit) {
        var best = initialSolution
        val (model, manager, summary) = initialSolution.problem.toProblem(matrix)

        val searchParameters = main.defaultRoutingSearchParameters().toBuilder()
            .setFirstSolutionStrategy(FirstSolutionStrategy.Value.AUTOMATIC)
            .setTimeLimit(Duration.newBuilder().setSeconds(config.timeLimit.toSeconds()).build())
            .setLocalSearchMetaheuristic(LocalSearchMetaheuristic.Value.GUIDED_LOCAL_SEARCH)
            .setLogSearch(true)
            .build()

        model.addAtSolutionCallback {
            if (isSolving()) {
                val actual = model.toDTO(manager, initialSolution.problem, summary.idxLocations, matrix)
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
                val sol = model.toDTO(manager, initialSolution.problem, summary.idxLocations, matrix, solution)
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