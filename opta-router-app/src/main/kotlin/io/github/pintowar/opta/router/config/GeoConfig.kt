package io.github.pintowar.opta.router.config

import io.github.pintowar.opta.router.adapters.geo.GraphHopperGeoService
import io.github.pintowar.opta.router.core.domain.ports.GeoService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GeoConfig {

    /**
     * The creation of the Graphhopper Wrapper.
     */
    @Bean
    fun graphHopper(
        @Value("\${app.graph.osm.path}") path: String,
        @Value("\${app.graph.osm.location}") location: String
    ): GeoService {
        return GraphHopperGeoService(path, location)
    }
}