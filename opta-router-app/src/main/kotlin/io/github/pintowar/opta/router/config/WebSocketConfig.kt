package io.github.pintowar.opta.router.config

import io.github.pintowar.opta.router.config.ConfigData.WEBSESSION_ID
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.server.WebSocketService
import org.springframework.web.reactive.socket.server.support.HandshakeWebSocketService
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter
import org.springframework.web.reactive.socket.server.upgrade.ReactorNettyRequestUpgradeStrategy
import java.util.function.Predicate

@Configuration
internal class WebSocketConfig {

    @Bean
    fun webSocketHandlerMapping(webSocketHandler: WebSocketHandler): HandlerMapping {
        val map = mapOf("/ws/solution-state/*" to webSocketHandler)
        return SimpleUrlHandlerMapping(map, 1)
    }

    @Bean
    fun handlerAdapter(webSocketService: WebSocketService) =
        WebSocketHandlerAdapter(webSocketService)

    @Bean
    fun webSocketService(): WebSocketService {
        return HandshakeWebSocketService(ReactorNettyRequestUpgradeStrategy()).apply {
            sessionAttributePredicate = Predicate {
                it == WEBSESSION_ID
            }
        }
    }
}