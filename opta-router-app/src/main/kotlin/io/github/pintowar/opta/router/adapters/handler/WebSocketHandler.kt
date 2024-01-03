package io.github.pintowar.opta.router.adapters.handler

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.pintowar.opta.router.config.ConfigData
import io.github.pintowar.opta.router.core.domain.models.VrpSolutionRequest
import io.github.pintowar.opta.router.core.serialization.Serde
import io.github.pintowar.opta.router.core.solver.SolverPanelStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.asPublisher
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import org.springframework.web.util.UriTemplate
import reactor.core.publisher.Mono

private val logger = KotlinLogging.logger {}

@Component
@Profile(ConfigData.REST_PROFILE)
class WebSocketHandler(
    private val solverPanelStorage: SolverPanelStorage,
    private val serde: Serde
) : WebSocketHandler {

    private val sharedFlow = MutableSharedFlow<VrpSolutionRequest>()
    private val uriTemplate = UriTemplate("/ws/solution-state/{instanceId}")

    override fun handle(session: WebSocketSession): Mono<Void> {
        val webSessionId = session.attributes[ConfigData.WEBSESSION_ID]!! as String

        val uriInstanceId = uriTemplate.match(session.handshakeInfo.uri.path)["instanceId"]
        val source = fromChannel(webSessionId, uriInstanceId)

        return session
            .send(source.map(session::textMessage).asPublisher())
            .doOnError { e ->
                logger.warn(e) { "WebSocket Error!!" }
            }
    }

    fun fromChannel(webSessionId: String, uriInstanceId: String?): Flow<String> {
        return sharedFlow
            .filter {
                "${it.solution.problem.id}" == uriInstanceId
            }
            .map { data ->
                val sol = solverPanelStorage.convertSolutionForPanelId(webSessionId, data.solution)
                serde.toJson(data.copy(solution = sol))
            }
    }

    suspend fun broadcast(data: VrpSolutionRequest) {
        sharedFlow.emit(data)
    }
}