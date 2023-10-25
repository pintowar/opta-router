package io.github.pintowar.opta.router.config

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

//import io.github.pintowar.opta.router.adapters.handler.WebSocketHandler
//import org.springframework.context.annotation.Configuration
//import org.springframework.web.socket.config.annotation.EnableWebSocket
//import org.springframework.web.socket.config.annotation.WebSocketConfigurer
//import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry
//import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor
//
//@Configuration
//@EnableWebSocket
//internal class WebSocketConfig(private val handler: WebSocketHandler) : WebSocketConfigurer {
//
//    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
//        val webSocketInterceptor = HttpSessionHandshakeInterceptor().apply {
//            isCreateSession = true
//            isCopyAllAttributes = true
//        }
//
//        registry.addHandler(handler, "/ws/solution-state/*")
//            .addInterceptors(webSocketInterceptor)
//            .setAllowedOrigins("*")
//    }
//}

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
                it == "websession-id"
            }
        }
    }
}