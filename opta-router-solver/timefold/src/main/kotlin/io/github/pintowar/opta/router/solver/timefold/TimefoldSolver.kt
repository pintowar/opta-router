package io.github.pintowar.opta.router.solver.timefold

import ai.timefold.solver.core.api.solver.SolverFactory
import ai.timefold.solver.core.config.solver.termination.TerminationConfig
import io.github.pintowar.opta.router.solver.timefold.domain.VehicleRoutingSolution
import io.github.pintowar.opta.router.core.domain.models.VrpSolution
import io.github.pintowar.opta.router.core.domain.models.matrix.Matrix
import io.github.pintowar.opta.router.core.solver.SolverConfig
import io.github.pintowar.opta.router.core.solver.spi.Solver
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import ai.timefold.solver.core.config.solver.SolverConfig as SC

class TimefoldSolver : Solver {

    private val configPath = "cvrp-timefold-config.xml"
    private val solverConfig = SC.createFromXmlResource(configPath)

    override val name: String = "timefold"

    override fun solutionFlow(initialSolution: VrpSolution, matrix: Matrix, config: SolverConfig): Flow<VrpSolution> {
        val solver = SolverFactory.create<VehicleRoutingSolution>(
            solverConfig.apply {
                terminationConfig = TerminationConfig().apply {
                    overwriteSpentLimit(config.timeLimit)
                }
            }
        ).buildSolver()

        return callbackFlow {
            solver.addEventListener { evt ->
                val sol = evt.newBestSolution
                trySendBlocking(sol.toDTO(initialSolution.problem, matrix))
            }

            val sol = suspendCancellableCoroutine<VehicleRoutingSolution> { continuation ->
                continuation.invokeOnCancellation { solver.terminateEarly() }
                continuation.resume(solver.solve(initialSolution.toSolverSolution(matrix)))
            }
            send(sol.toDTO(initialSolution.problem, matrix))
            close()
        }
    }
}