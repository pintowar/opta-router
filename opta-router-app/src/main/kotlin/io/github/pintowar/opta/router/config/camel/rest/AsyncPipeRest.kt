package io.github.pintowar.opta.router.config.camel.rest

import io.github.pintowar.opta.router.adapters.handler.WebSocketHandler
import io.github.pintowar.opta.router.config.ConfigData
import io.github.pintowar.opta.router.core.domain.messages.SolutionCommand
import io.github.pintowar.opta.router.core.domain.messages.SolutionRequestCommand
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

    fun update(cmd: SolutionRequestCommand): Publisher<SolutionCommand> {
        return publish {
            send(SolutionCommand(solverService.update(cmd.solutionRequest, cmd.clear)))
        }
    }

    fun broadcast(cmd: SolutionCommand): Publisher<Unit> {
        return publish {
            webSocketHandler.broadcast(cmd.solutionRequest)
        }
    }
}