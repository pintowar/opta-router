package io.github.pintowar.opta.router.config.geo

import io.github.pintowar.opta.router.config.ConfigData
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.MediaType
import org.springframework.messaging.rsocket.RSocketStrategies
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler

@Configuration
@Profile(ConfigData.GEO_SERVER_PROFILE)
class RSocketHandlerConfig {

    @Bean
    fun rsocketMessageHandler(strategies: RSocketStrategies) = RSocketMessageHandler().apply {
        defaultDataMimeType = MediaType.APPLICATION_CBOR
        rSocketStrategies = strategies
    }
}