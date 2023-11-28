package io.github.pintowar.opta.router.config

import io.github.pintowar.opta.router.adapters.handler.WebSocketHandler
import io.github.pintowar.opta.router.core.domain.models.VrpSolutionRequest
import io.github.pintowar.opta.router.core.solver.VrpSolverService
import kotlinx.coroutines.reactive.publish
import org.reactivestreams.Publisher
import org.springframework.stereotype.Component

@Component
class AsyncPipe(
    private val solverService: VrpSolverService,
    private val webSocketHandler: WebSocketHandler
) {

    fun updateAndBroadcast(solRequest: VrpSolutionRequest, clear: Boolean): Publisher<Unit> {
        return publish {
            solverService.updateAndBroadcast(solRequest, clear)
        }
    }

    fun broadcast(data: VrpSolutionRequest): Publisher<Unit> {
        return publish {
            webSocketHandler.broadcast(data)
        }
    }
}