package com.github

import com.github.service.impl.WebSocketHandler
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor


@Configuration
@EnableWebSocket
internal class WebSocketConfig(private val handler: WebSocketHandler) : WebSocketConfigurer {

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        val webSocketInterceptor = HttpSessionHandshakeInterceptor().apply { isCreateSession = true }

        registry.addHandler(handler, "/solution-state")
                .addInterceptors(webSocketInterceptor)
                .setAllowedOrigins("*")
    }
}