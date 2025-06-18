package io.github.pintowar.opta.router.config.geo

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.pintowar.opta.router.adapters.geo.GraphHopperGeoAdapter
import io.github.pintowar.opta.router.config.ConfigData
import io.github.pintowar.opta.router.config.SerdeConfig
import io.github.pintowar.opta.router.core.domain.ports.service.GeoPort
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.MediaType
import org.springframework.http.codec.cbor.Jackson2CborDecoder
import org.springframework.http.codec.cbor.Jackson2CborEncoder
import org.springframework.messaging.rsocket.RSocketStrategies

@Configuration
@Profile(ConfigData.GEO_LOCAL_PROFILE, ConfigData.GEO_SERVER_PROFILE)
class GeoConfig {
    /**
     * The creation of the Graphhopper Wrapper.
     */
    @Bean
    fun graphHopper(
        @Value("\${app.graph.osm.path}") path: String,
        @Value("\${app.graph.osm.location}") location: String
    ): GeoPort = GraphHopperGeoAdapter(path, location)

    @Bean
    fun rsocketStrategies(objectMapper: ObjectMapper): RSocketStrategies {
        val cborMapper = SerdeConfig.cborMapperFromObjectMapper(objectMapper)
        return RSocketStrategies
            .builder()
            .encoder(Jackson2CborEncoder(cborMapper, MediaType.APPLICATION_CBOR))
            .decoder(Jackson2CborDecoder(cborMapper, MediaType.APPLICATION_CBOR))
            .build()
    }
}