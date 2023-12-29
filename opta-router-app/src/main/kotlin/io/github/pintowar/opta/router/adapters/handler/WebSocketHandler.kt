package io.github.pintowar.opta.router.adapters.handler

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.pintowar.opta.router.config.ConfigData
import io.github.pintowar.opta.router.core.domain.models.SolverPanel
import io.github.pintowar.opta.router.core.domain.models.VrpSolutionRequest
import io.github.pintowar.opta.router.core.domain.ports.GeoPort
import io.github.pintowar.opta.router.core.serde.Serde
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.asPublisher
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import org.springframework.web.util.UriTemplate
import reactor.core.publisher.Mono
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger {}

@Component
class WebSocketHandler(
    private val sessionPanel: MutableMap<String, SolverPanel>,
    private val serde: Serde,
    private val geoService: GeoPort
) : WebSocketHandler {

    private val sessions: MutableMap<String, WebSocketSession> = ConcurrentHashMap()
    private val sharedFlow = MutableSharedFlow<VrpSolutionRequest>()
    private val uriTemplate = UriTemplate("/ws/solution-state/{instanceId}")

    override fun handle(session: WebSocketSession): Mono<Void> {
        val webSessionId = session.attributes[ConfigData.WEBSESSION_ID]!! as String
        sessions[webSessionId] = session

        val uriInstanceId = uriTemplate.match(session.handshakeInfo.uri.path)["instanceId"]
        val source = fromChannel(webSessionId, uriInstanceId)

        return session
            .send(source.map(session::textMessage).asPublisher())
            .doOnError { e ->
                logger.warn(e) { "WebSocket Error!!" }
            }
            .doFinally {
                sessions.remove(session.id)
            }
    }

    fun fromChannel(webSessionId: String, uriInstanceId: String?): Flow<String> {
        return sharedFlow
            .filter {
                "${it.solution.problem.id}" == uriInstanceId
            }
            .map { data ->
                val panel = sessionPanel[webSessionId] ?: SolverPanel()
                val sol = if (panel.isDetailedPath) geoService.detailedPaths(data.solution) else data.solution
                serde.toJson(data.copy(solution = sol))
            }
    }

    fun broadcast(data: VrpSolutionRequest) {
        runBlocking {
            sharedFlow.emit(data)
        }
    }
}