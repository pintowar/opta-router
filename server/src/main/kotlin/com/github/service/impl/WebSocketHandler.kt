package com.github.service.impl

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.service.NotificationService
import com.github.vrp.VrpSolutionState
import mu.KLogging
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import org.springframework.web.util.UriTemplate
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap


@Component
class WebSocketHandler(private val mapper: ObjectMapper) : TextWebSocketHandler(), NotificationService {

    companion object : KLogging()

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
        val textData = mapper.writeValueAsString(data)
        sessions.forEach { (_, session) ->
            notifyUser(session, data.solution.instanceId, textData)
        }
    }

    private fun notifyUser(session: WebSocketSession, instanceId: Long, data: String) {
        try {
            val template = UriTemplate("/ws/solution-state/{instanceId}")
            val uriInstanceId = template.match(session.uri!!.path)["instanceId"]
            if ("$instanceId" == uriInstanceId) {
                session.sendMessage(TextMessage(data))
            }
        } catch (e: IOException) {
            logger.error("Could not send message message through web socket!", e)
        }
    }

    override fun broadcastSolution(data: VrpSolutionState) {
        logger.info("{}, {}", data.state, data.solution.getTotalDistance().toString())
        broadcast(data)
    }
}