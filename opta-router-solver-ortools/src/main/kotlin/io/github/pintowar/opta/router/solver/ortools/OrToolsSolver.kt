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
import io.github.pintowar.opta.router.core.solver.SolutionFlow
import io.github.pintowar.opta.router.core.solver.SolverConfig
import io.github.pintowar.opta.router.core.solver.spi.Solver
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import java.util.UUID

class OrToolsSolver : Solver {

    init {
        Loader.loadNativeLibraries()
    }

    override val name: String = "or-tools"

    override fun solutionFlow(initialSolution: VrpSolution, matrix: Matrix, config: SolverConfig): Flow<SolutionFlow> {
        return callbackFlow {
            val ctx = currentCoroutineContext()
            val (model, manager, summary) = initialSolution.problem.toProblem(matrix)

            val searchParameters = main.defaultRoutingSearchParameters().toBuilder()
                .setFirstSolutionStrategy(FirstSolutionStrategy.Value.AUTOMATIC)
                .setTimeLimit(Duration.newBuilder().setSeconds(config.timeLimit.toSeconds()).build())
                .setLocalSearchMetaheuristic(LocalSearchMetaheuristic.Value.GUIDED_LOCAL_SEARCH)
                .build()

            model.addAtSolutionCallback {
                if (ctx.isActive) {
                    val actual = model.toDTO(manager, initialSolution.problem, summary.idxLocations, matrix)
                    trySendBlocking(SolutionFlow(actual))
                } else {
                    model.solver().finishCurrentSearch()
                }
            }

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
                    send(SolutionFlow(sol, true))
                } else {
                    throw IllegalStateException("Couldn't find an optimal solution")
                }
            } finally {
                model.delete()
                close()
            }
        }
    }
}