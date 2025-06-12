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
    fun solve(cmd: RequestSolverCommand): Publisher<SolutionRequestCommand> {
        return solverManager.solve(cmd.solverKey, cmd.detailedSolution, cmd.solverName).asPublisher()
    }

    fun cancelSolver(cmd: CancelSolverCommand): Publisher<Unit> {
        return publish {
            solverManager.cancelSolver(cmd.solverKey, cmd.currentStatus, cmd.clear)
        }
    }
}