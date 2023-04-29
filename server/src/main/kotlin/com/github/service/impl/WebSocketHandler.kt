package com.github.service.impl

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.service.NotificationService
import com.github.vrp.SolverState
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

    override fun broadcastSolution(solverState: SolverState?, newBestSolution: VrpSolution) {
        val data = mapOf(
//                "id" to newBestSolution.id,
                "solution" to newBestSolution,
                "status" to solverState
        )
        logger.info("{}, {}", solverState, newBestSolution.getTotalDistance().toString())
        broadcast(mapper.writeValueAsString(data))
    }
}