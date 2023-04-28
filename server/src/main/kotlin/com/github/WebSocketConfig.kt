package com.github

import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry


@Configuration
@EnableWebSocket
internal class WebSocketConfig(private val handler: WebSocketNotification) : WebSocketConfigurer {
    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(handler, "/solution-state/*")
                .setAllowedOrigins("*")
    }

//    @Bean
//    fun solutionNotificationHandler(mapper: ObjectMapper): WebSocketNotification {
//        return WebSocketNotification(mapper)
//    }
}