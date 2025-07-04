package io.github.pintowar.opta.router.solver.optaplanner

import io.github.pintowar.opta.router.core.domain.models.VrpSolution
import io.github.pintowar.opta.router.core.domain.models.matrix.Matrix
import io.github.pintowar.opta.router.core.solver.SolverConfig
import io.github.pintowar.opta.router.core.solver.spi.Solver
import io.github.pintowar.opta.router.solver.optaplanner.domain.VehicleRoutingSolution
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import org.optaplanner.core.api.solver.SolverFactory
import org.optaplanner.core.config.solver.termination.TerminationConfig
import kotlin.coroutines.resume
import org.optaplanner.core.config.solver.SolverConfig as SC

class OptaSolver : Solver() {
    private val configPath = "cvrp-optaplanner-config.xml"
    private val solverConfig = SC.createFromXmlResource(configPath)

    override val name: String = "optaplanner"

    /**
     * Solves the VRP problem using OptaPlanner and emits solutions as they are found.
     *
     * This function configures an OptaPlanner solver with a time limit and an event listener
     * to emit the best solutions found during the solving process. It starts the solver with
     * an initial solution and continues until the time limit is reached or the coroutine is cancelled.
     *
     * @param initialSolution The initial [VrpSolution] to start the solver from.
     * @param matrix The [Matrix] containing travel distances between locations.
     * @param config The [SolverConfig] containing parameters like the time limit for the solver.
     * @return A [Flow] of [VrpSolution] objects, representing the best solution found at different stages of the solving process.
     */
    override fun solveFlow(
        initialSolution: VrpSolution,
        matrix: Matrix,
        config: SolverConfig
    ): Flow<VrpSolution> {
        val solver =
            SolverFactory
                .create<VehicleRoutingSolution>(
                    solverConfig.apply {
                        terminationConfig =
                            TerminationConfig().apply {
                                overwriteSpentLimit(config.timeLimit)
                            }
                    }
                ).buildSolver()

        return callbackFlow {
            solver.addEventListener { evt ->
                val sol = evt.newBestSolution
                trySendBlocking(sol.toDTO(initialSolution.problem, matrix))
            }

            val sol =
                suspendCancellableCoroutine<VehicleRoutingSolution> { continuation ->
                    continuation.invokeOnCancellation { solver.terminateEarly() }
                    continuation.resume(solver.solve(initialSolution.toSolverSolution(matrix)))
                }
            send(sol.toDTO(initialSolution.problem, matrix))
            close()
        }
    }
}