package io.github.pintowar.opta.router.adapters.handler

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.pintowar.opta.router.core.domain.models.SolverPanel
import io.github.pintowar.opta.router.core.domain.models.VrpSolutionRequest
import io.github.pintowar.opta.router.core.domain.ports.GeoPort
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
    private val geoService: GeoPort
) : TextWebSocketHandler() {

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

    fun broadcast(data: VrpSolutionRequest) {
        val cache = mutableMapOf<Boolean, String>()
        sessions.forEach { (sessionId, session) ->
            val panel = sessionPanel[sessionId] ?: SolverPanel()

            val textData = cache.computeIfAbsent(panel.isDetailedPath) {
                val sol = if (it) geoService.detailedPaths(data.solution) else data.solution
                mapper.writeValueAsString(data.copy(solution = sol))
            }

            notifyUser(session, data.solution.problem.id, textData)
        }
    }

    private fun sessionIdFromSession(session: WebSocketSession): String {
        return session.attributes["HTTP.SESSION.ID"]!!.toString()
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
}