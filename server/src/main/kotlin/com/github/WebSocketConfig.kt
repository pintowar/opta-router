package com.github

import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.stereotype.Service
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.messaging.SessionConnectEvent
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor
import java.util.concurrent.ConcurrentHashMap


@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig : AbstractWebSocketMessageBrokerConfigurer() {

    override fun configureMessageBroker(config: MessageBrokerRegistry) {
        config.enableSimpleBroker("/queue")
//        config.enableStompBrokerRelay("/queue")
        config.setApplicationDestinationPrefixes("/app")
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        val inter = HttpSessionHandshakeInterceptor()
        inter.isCreateSession = true
        registry.addEndpoint("/stomp").setAllowedOrigins("*").withSockJS()
                .setInterceptors(HttpSessionHandshakeInterceptor())
    }
}

@Service
class StompConnectEventListener(val sessionWebSocket: ConcurrentHashMap<String, String>) : ApplicationListener<SessionConnectEvent> {

    override fun onApplicationEvent(event: SessionConnectEvent) {
        val sha = StompHeaderAccessor.wrap(event.message)
        val httpSession = sha.sessionAttributes[HttpSessionHandshakeInterceptor.HTTP_SESSION_ID_ATTR_NAME] as String
        val socketSession = sha.sessionId
        LOGGER.info("HttpSession ID: {}, Socket Session ID: {}", httpSession, socketSession)
        sessionWebSocket[httpSession] = sha.sessionId
    }

    companion object {
        internal val LOGGER = LoggerFactory.getLogger(StompConnectEventListener::class.java)
    }
}