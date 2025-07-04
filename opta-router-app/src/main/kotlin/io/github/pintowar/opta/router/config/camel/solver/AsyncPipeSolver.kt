package io.github.pintowar.opta.router.config.camel.solver

import io.github.pintowar.opta.router.config.ConfigData
import io.github.pintowar.opta.router.core.domain.messages.CancelSolverCommand
import io.github.pintowar.opta.router.core.domain.messages.RequestSolverCommand
import io.github.pintowar.opta.router.core.domain.messages.SolutionRequestCommand
import io.github.pintowar.opta.router.core.solver.VrpSolverManager
import kotlinx.coroutines.reactive.asPublisher
import kotlinx.coroutines.reactive.publish
import org.reactivestreams.Publisher
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile(ConfigData.SOLVER_PROFILE)
class AsyncPipeSolver(
    private val solverManager: VrpSolverManager
) {
    /**
     * Solves the VRP problem.
     *
     * @param cmd The request solver command.
     * @return A `Publisher` that emits `SolutionRequestCommand`s.
     */
    fun solve(cmd: RequestSolverCommand): Publisher<SolutionRequestCommand> =
        solverManager.solve(cmd.solverKey, cmd.detailedSolution, cmd.solverName).asPublisher()

    /**
     * Cancels the solver.
     *
     * @param cmd The cancel solver command.
     * @return A `Publisher` that completes when the solver is canceled.
     */
    fun cancelSolver(cmd: CancelSolverCommand): Publisher<Unit> =
        publish {
            solverManager.cancelSolver(cmd.solverKey, cmd.currentStatus, cmd.clear)
        }
}