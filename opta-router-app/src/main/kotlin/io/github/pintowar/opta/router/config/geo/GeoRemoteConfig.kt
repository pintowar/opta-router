package io.github.pintowar.opta.router.config.geo

import io.github.pintowar.opta.router.config.ConfigData
import io.github.pintowar.opta.router.core.domain.models.Coordinate
import io.github.pintowar.opta.router.core.domain.models.Customer
import io.github.pintowar.opta.router.core.domain.models.Depot
import io.github.pintowar.opta.router.core.domain.models.LatLng
import io.github.pintowar.opta.router.core.domain.models.Location
import io.github.pintowar.opta.router.core.domain.models.Path
import io.github.pintowar.opta.router.core.domain.models.Route
import io.github.pintowar.opta.router.core.domain.models.matrix.VrpProblemMatrix
import io.github.pintowar.opta.router.core.domain.ports.service.GeoPort
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

@Configuration
@Profile(ConfigData.GEO_REMOTE_PROFILE)
class GeoRemoteConfig {

    /**
     * The creation of the Graphhopper Client.
     */
    @Bean
    fun graphHopper(
        webClient: WebClient
    ): GeoPort = object : GeoPort {
        override suspend fun simplePath(origin: Coordinate, target: Coordinate): Path {
            data class SimplePathRequest(val origin: LatLng, val target: LatLng)

            return webClient.post()
                .uri("/api/geo/simple-path")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(
                    SimplePathRequest(LatLng(origin.lat, origin.lng), LatLng(target.lat, target.lng))
                )
                .retrieve()
                .awaitBody()
        }

        override suspend fun generateMatrix(locations: Set<Location>): VrpProblemMatrix {
            data class LocationsRequest(val depots: List<Depot>, val customers: List<Customer>)

            return webClient.post()
                .uri("/api/geo/generate-matrix")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(
                    LocationsRequest(locations.filterIsInstance<Depot>(), locations.filterIsInstance<Customer>())
                )
                .retrieve()
                .awaitBody()
        }

        override suspend fun detailedPaths(routes: List<Route>): List<Route> {
            data class DetailedPathRequest(val routes: List<Route>)

            return webClient.post()
                .uri("/api/geo/detailed-paths")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(DetailedPathRequest(routes))
                .retrieve()
                .awaitBody()
        }
    }

    @Bean
    fun webClient(@Value("\${app.geo.remote.url}") url: String): WebClient {
        return WebClient.builder()
            .codecs { cfg -> cfg.defaultCodecs().maxInMemorySize(100 * 1024 * 1024) }
            .baseUrl(url)
            .build()
    }
}