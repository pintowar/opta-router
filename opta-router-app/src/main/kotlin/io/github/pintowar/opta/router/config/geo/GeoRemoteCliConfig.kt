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
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.messaging.rsocket.RSocketStrategies
import org.springframework.messaging.rsocket.retrieveAndAwait
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import java.net.URI

@Configuration
@Profile(ConfigData.GEO_REMOTE_PROFILE)
class GeoRemoteCliConfig {
    /**
     * The creation of the Graphhopper Web Client.
     */
    @Bean
    fun graphHopper(
        @Value("\${app.geo.remote.uri}") uri: URI,
        strategies: RSocketStrategies
    ): GeoPort =
        when (uri.scheme.lowercase()) {
            in listOf("http", "https") -> generateGeoWebCli(uri)
            in listOf("tcp") -> generateRSocketWebCli(uri, strategies)
            else -> throw IllegalArgumentException("Invalid remote geo uri ($uri). Must start with http, https or tcp")
        }

    private fun generateGeoWebCli(uri: URI): GeoPort =
        object : GeoPort {
            private val webClient =
                WebClient
                    .builder()
                    .codecs { cfg -> cfg.defaultCodecs().maxInMemorySize(100 * 1024 * 1024) }
                    .baseUrl(uri.toString())
                    .build()

            override suspend fun simplePath(
                origin: Coordinate,
                target: Coordinate
            ): Path {
                data class SimplePathRequest(
                    val origin: LatLng,
                    val target: LatLng
                )

                return webClient
                    .post()
                    .uri("/api/geo/simple-path")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(
                        SimplePathRequest(LatLng(origin.lat, origin.lng), LatLng(target.lat, target.lng))
                    ).retrieve()
                    .awaitBody()
            }

            override suspend fun generateMatrix(locations: Set<Location>): VrpProblemMatrix {
                data class LocationsRequest(
                    val depots: List<Depot>,
                    val customers: List<Customer>
                )

                return webClient
                    .post()
                    .uri("/api/geo/generate-matrix")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(
                        LocationsRequest(locations.filterIsInstance<Depot>(), locations.filterIsInstance<Customer>())
                    ).retrieve()
                    .awaitBody()
            }

            override suspend fun detailedPaths(routes: List<Route>): List<Route> {
                data class DetailedPathRequest(
                    val routes: List<Route>
                )

                return webClient
                    .post()
                    .uri("/api/geo/detailed-paths")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(DetailedPathRequest(routes))
                    .retrieve()
                    .awaitBody()
            }
        }

    private fun generateRSocketWebCli(
        uri: URI,
        strategies: RSocketStrategies
    ): GeoPort =
        object : GeoPort {
            private val requester =
                RSocketRequester
                    .builder()
                    .dataMimeType(MediaType.APPLICATION_CBOR)
                    .rsocketStrategies(strategies)
                    .tcp(uri.host, uri.port)

            override suspend fun simplePath(
                origin: Coordinate,
                target: Coordinate
            ): Path {
                data class SimplePathRequest(
                    val origin: LatLng,
                    val target: LatLng
                )

                return requester
                    .route("simple.path")
                    .data(SimplePathRequest(LatLng(origin.lat, origin.lng), LatLng(target.lat, target.lng)))
                    .retrieveAndAwait()
            }

            override suspend fun generateMatrix(locations: Set<Location>): VrpProblemMatrix {
                data class LocationsRequest(
                    val depots: List<Depot>,
                    val customers: List<Customer>
                )

                return requester
                    .route("generate.matrix")
                    .data(
                        LocationsRequest(locations.filterIsInstance<Depot>(), locations.filterIsInstance<Customer>())
                    ).retrieveAndAwait()
            }

            override suspend fun detailedPaths(routes: List<Route>): List<Route> {
                data class DetailedPathRequest(
                    val routes: List<Route>
                )

                return requester
                    .route("detailed.paths")
                    .data(DetailedPathRequest(routes))
                    .retrieveAndAwait()
            }
        }
}