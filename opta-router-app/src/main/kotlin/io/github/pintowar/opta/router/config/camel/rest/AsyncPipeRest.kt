package io.github.pintowar.opta.router.config.camel.rest

import io.github.pintowar.opta.router.adapters.handler.WebSocketHandler
import io.github.pintowar.opta.router.config.ConfigData
import io.github.pintowar.opta.router.core.domain.ports.BroadcastPort
import io.github.pintowar.opta.router.core.domain.ports.SolverEventsPort
import io.github.pintowar.opta.router.core.solver.VrpSolverService
import kotlinx.coroutines.reactive.publish
import org.reactivestreams.Publisher
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile(ConfigData.REST_PROFILE)
class AsyncPipeRest(
    private val solverService: VrpSolverService,
    private val webSocketHandler: WebSocketHandler
) {

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