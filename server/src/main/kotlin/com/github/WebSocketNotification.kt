package com.github

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.vrp.Status
import com.github.vrp.VrpSolution
import mu.KLogging
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap


@Component
class WebSocketNotification(private val mapper: ObjectMapper) : TextWebSocketHandler() {

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
        val path = session.uri!!.path
        return path.substring(path.lastIndexOf('/') + 1)
    }

    private fun broadcast(data: String) {
        sessions.keys.forEach { sessionId ->
            notifyUser(sessionId, data)
        }
    }

    private fun notifyUser(sessionId: String, data: String) {
        try {
            sessions[sessionId]!!.sendMessage(TextMessage(data))
        } catch (e: IOException) {
            logger.error("Could not send message message through web socket!", e)
        }
    }

    fun notifyUserInvestmentChange(sessionId: String, status: Status?, newBestSolution: VrpSolution) {
        val data = mapOf(
//                "id" to newBestSolution.getId(),
                "solution" to newBestSolution,
                "status" to status
        )
        logger.info("{}, {}", status, newBestSolution.getTotalDistance().toString())
        notifyUser(sessionId, mapper.writeValueAsString(data))
    }
}