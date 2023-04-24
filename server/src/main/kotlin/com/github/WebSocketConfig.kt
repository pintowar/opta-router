package com.github

import mu.KLogging
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.stereotype.Service
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer
import org.springframework.web.socket.messaging.SessionConnectEvent
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor
import java.util.concurrent.ConcurrentHashMap


/**
 * This class contains the WS configuration.
 */
@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig : WebSocketMessageBrokerConfigurer {

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        registry.enableSimpleBroker("/queue")
        registry.setApplicationDestinationPrefixes("/app")
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/stomp").setAllowedOriginPatterns("*").withSockJS()
                .setInterceptors(HttpSessionHandshakeInterceptor())
    }

}

/**
 * This event listener associates the Socket Session ID to the Http Session ID at the connection event of the WebSocket.
 * The association is stored at the sessionWebSocket ConcurrentHashMap. This association is needed for personalized
 * notification via WS.
 */
@Service
class StompConnectEventListener(val sessionWebSocket: ConcurrentHashMap<String, String>) : ApplicationListener<SessionConnectEvent> {

    companion object : KLogging()

    override fun onApplicationEvent(event: SessionConnectEvent) {
        val sha = StompHeaderAccessor.wrap(event.message)
        val httpSession = sha.sessionAttributes?.get(HttpSessionHandshakeInterceptor.HTTP_SESSION_ID_ATTR_NAME)!! as String
        val socketSession = sha.sessionId
        logger.info("HttpSession ID: {}, Socket Session ID: {}", httpSession, socketSession)
        sessionWebSocket[httpSession] = socketSession!!
    }
}