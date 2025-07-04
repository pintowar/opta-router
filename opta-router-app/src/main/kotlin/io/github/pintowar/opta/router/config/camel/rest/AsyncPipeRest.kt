package io.github.pintowar.opta.router.config.camel.rest

import io.github.pintowar.opta.router.adapters.handler.WebSocketHandler
import io.github.pintowar.opta.router.config.ConfigData
import io.github.pintowar.opta.router.core.domain.messages.SolutionCommand
import io.github.pintowar.opta.router.core.domain.messages.SolutionRequestCommand
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
    /**
     * Updates the solver with a new solution request.
     *
     * @param cmd The solution request command.
     * @return A `Publisher` that emits a `SolutionCommand` with the updated solution.
     */
    fun update(cmd: SolutionRequestCommand): Publisher<SolutionCommand> =
        publish {
            send(SolutionCommand(solverService.update(cmd.solutionRequest, cmd.clear)))
        }

    /**
     * Broadcasts the solution command to all connected clients.
     *
     * @param cmd The solution command to broadcast.
     * @return A `Publisher` that completes when the broadcast is done.
     */
    fun broadcast(cmd: SolutionCommand): Publisher<Unit> =
        publish {
            webSocketHandler.broadcast(cmd.solutionRequest)
        }
}