package io.github.pintowar.opta.router.config

import io.github.pintowar.opta.router.adapters.handler.WebSocketHandler
import io.github.pintowar.opta.router.core.domain.ports.BroadcastPort
import io.github.pintowar.opta.router.core.domain.ports.SolverEventsPort
import io.github.pintowar.opta.router.core.solver.VrpSolverManager
import io.github.pintowar.opta.router.core.solver.VrpSolverService
import kotlinx.coroutines.reactive.asPublisher
import kotlinx.coroutines.reactive.publish
import org.reactivestreams.Publisher
import org.springframework.stereotype.Component

@Component
class AsyncPipe(
    private val solverService: VrpSolverService,
    private val webSocketHandler: WebSocketHandler,
    private val solverManager: VrpSolverManager
) {

    fun solve(cmd: SolverEventsPort.RequestSolverCommand): Publisher<SolverEventsPort.SolutionRequestCommand> {
        return solverManager.solve(cmd.solverKey, cmd.detailedSolution, cmd.solverName).asPublisher()
    }

    fun cancelSolver(cmd: SolverEventsPort.CancelSolverCommand): Publisher<Unit> {
        return publish {
            solverManager.cancelSolver(cmd.solverKey, cmd.currentStatus, cmd.clear)
        }
    }

    fun update(cmd: SolverEventsPort.SolutionRequestCommand): Publisher<BroadcastPort.SolutionCommand> {
        return publish {
            send(BroadcastPort.SolutionCommand(solverService.update(cmd.solutionRequest, cmd.clear)))
        }
    }

    fun broadcast(cmd: BroadcastPort.SolutionCommand): Publisher<Unit> {
        return publish {
            webSocketHandler.broadcast(cmd.solutionRequest)
        }
    }
}