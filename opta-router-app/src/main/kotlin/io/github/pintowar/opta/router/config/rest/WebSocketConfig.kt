package io.github.pintowar.opta.router.config.rest

import io.github.pintowar.opta.router.config.ConfigData
import io.github.pintowar.opta.router.config.ConfigData.WEBSESSION_ID
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.server.WebSocketService
import org.springframework.web.reactive.socket.server.support.HandshakeWebSocketService
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter
import org.springframework.web.reactive.socket.server.upgrade.ReactorNettyRequestUpgradeStrategy
import java.util.function.Predicate

/**
 * This class is responsible for configuring the WebSocket.
 */
@Configuration
@Profile(ConfigData.REST_PROFILE)
internal class WebSocketConfig {
    /**
     * Creates a handler mapping for the WebSocket.
     *
     * @param webSocketHandler The WebSocket handler.
     * @return The handler mapping.
     */
    @Bean
    fun webSocketHandlerMapping(webSocketHandler: WebSocketHandler): HandlerMapping {
        val map = mapOf("/ws/solution-state/*" to webSocketHandler)
        return SimpleUrlHandlerMapping(map, 1)
    }

    /**
     * Creates a handler adapter for the WebSocket.
     *
     * @param webSocketService The WebSocket service.
     * @return The handler adapter.
     */
    @Bean
    fun handlerAdapter(webSocketService: WebSocketService) = WebSocketHandlerAdapter(webSocketService)

    /**
     * Creates a WebSocket service.
     *
     * @return The WebSocket service.
     */
    @Bean
    fun webSocketService(): WebSocketService =
        HandshakeWebSocketService(ReactorNettyRequestUpgradeStrategy()).apply {
            sessionAttributePredicate =
                Predicate {
                    it == WEBSESSION_ID
                }
        }
}