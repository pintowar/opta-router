package io.github.pintowar.opta.router.solver.ortools

import com.google.ortools.Loader
import com.google.ortools.constraintsolver.FirstSolutionStrategy
import com.google.ortools.constraintsolver.LocalSearchMetaheuristic
import com.google.ortools.constraintsolver.main
import com.google.protobuf.Duration
import io.github.pintowar.opta.router.core.domain.models.SolverStatus
import io.github.pintowar.opta.router.core.domain.models.VrpSolution
import io.github.pintowar.opta.router.core.domain.models.VrpSolutionRequest
import io.github.pintowar.opta.router.core.domain.models.matrix.Matrix
import io.github.pintowar.opta.router.core.solver.SolverConfig
import io.github.pintowar.opta.router.core.solver.spi.Solver
import java.util.UUID

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
        if (!initialSolution.isEmpty()) callback(VrpSolutionRequest(initialSolution, SolverStatus.RUNNING, key))
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
            } else {
                throw IllegalStateException("Couldn't find an optimal solution")
            }
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