package io.github.pintowar.opta.router.adapters.handler

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.pintowar.opta.router.core.domain.models.SolverPanel
import io.github.pintowar.opta.router.core.domain.models.VrpSolutionState
import io.github.pintowar.opta.router.core.domain.ports.BroadcastService
import io.github.pintowar.opta.router.core.domain.ports.GeoService
import io.github.pintowar.opta.router.core.solver.pathPlotted
import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import org.springframework.web.util.UriTemplate
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger {}

@Component
class WebSocketHandler(
    private val sessionPanel: MutableMap<String, SolverPanel>,
    private val mapper: ObjectMapper,
    private val geoService: GeoService
) : TextWebSocketHandler(), BroadcastService {

    private val sessions: MutableMap<String, WebSocketSession> = ConcurrentHashMap()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val sessionId = sessionIdFromSession(session)
        sessions[sessionId] = session
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        val sessionId = sessionIdFromSession(session)
        sessions.remove(sessionId)
    }

    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        val sessionId = sessionIdFromSession(session)
        sessions.remove(sessionId)
    }

    private fun sessionIdFromSession(session: WebSocketSession): String {
        return session.attributes["HTTP.SESSION.ID"]!!.toString()
    }

    private fun broadcast(data: VrpSolutionState) {
        sessions.forEach { (sessionId, session) ->
            val panel = sessionPanel[sessionId] ?: SolverPanel()

            // TODO: cache resp to avoid doing this multiple times
            val textData =
                (if (panel.isDetailedPath) data.solution.pathPlotted(geoService, true) else data.solution).let {
                    mapper.writeValueAsString(data.copy(solution = it))
                }
            notifyUser(session, data.solution.instance.id, textData)
        }
    }

    private fun notifyUser(session: WebSocketSession, instanceId: Long, data: String) {
        try {
            val template = UriTemplate("/ws/solution-state/{instanceId}")
            val uriInstanceId = template.match(session.uri!!.path)["instanceId"]
            if ("$instanceId" == uriInstanceId) {
                session.sendMessage(TextMessage(data))
            }
        } catch (e: Exception) {
            logger.warn("Could not send message message through web socket!", e)
        }
    }

    override fun broadcastSolution(data: VrpSolutionState) {
        logger.info("{}, {}", data.state, data.solution.getTotalDistance().toString())
        broadcast(data)
    }
}