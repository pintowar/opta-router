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
     *
     * @param uri The URI of the remote geo service.
     * @param strategies The RSocket strategies.
     * @return The `GeoPort` implementation.
     */
    @Bean
    fun graphHopper(
        @Value($$"${app.geo.remote.uri}") uri: URI,
        strategies: RSocketStrategies
    ): GeoPort =
        when (uri.scheme.lowercase()) {
            in listOf("http", "https") -> generateGeoWebCli(uri)
            in listOf("tcp") -> generateRSocketWebCli(uri, strategies)
            else -> throw IllegalArgumentException("Invalid remote geo uri ($uri). Must start with http, https or tcp")
        }

    /**
     * Generates a `GeoPort` implementation that uses a WebClient.
     *
     * @param uri The URI of the remote geo service.
     * @return The `GeoPort` implementation.
     */
    private fun generateGeoWebCli(uri: URI): GeoPort =
        object : GeoPort {
            private val webClient =
                WebClient
                    .builder()
                    .codecs { cfg -> cfg.defaultCodecs().maxInMemorySize(100 * 1024 * 1024) }
                    .baseUrl(uri.toString())
                    .build()

            /**
             * Calculates the simple path between two coordinates.
             *
             * @param origin The origin coordinate.
             * @param target The target coordinate.
             * @return The path between the two coordinates.
             */
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

            /**
             * Generates a VRP problem matrix for a set of locations.
             *
             * @param locations The set of locations.
             * @return The VRP problem matrix.
             */
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

            /**
             * Calculates the detailed paths for a list of routes.
             *
             * @param routes The list of routes.
             * @return The list of routes with detailed paths.
             */
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

    /**
     * Generates a `GeoPort` implementation that uses an RSocket client.
     *
     * @param uri The URI of the remote geo service.
     * @param strategies The RSocket strategies.
     * @return The `GeoPort` implementation.
     */
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

            /**
             * Calculates the simple path between two coordinates.
             *
             * @param origin The origin coordinate.
             * @param target The target coordinate.
             * @return The path between the two coordinates.
             */
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

            /**
             * Generates a VRP problem matrix for a set of locations.
             *
             * @param locations The set of locations.
             * @return The VRP problem matrix.
             */
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

            /**
             * Calculates the detailed paths for a list of routes.
             *
             * @param routes The list of routes.
             * @return The list of routes with detailed paths.
             */
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