package io.github.pintowar.opta.router.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.pintowar.opta.router.config.ConfigData
import io.github.pintowar.opta.router.core.domain.models.Customer
import io.github.pintowar.opta.router.core.domain.models.Depot
import io.github.pintowar.opta.router.core.domain.models.LatLng
import io.github.pintowar.opta.router.core.domain.models.Path
import io.github.pintowar.opta.router.core.domain.models.Route
import io.github.pintowar.opta.router.core.domain.models.matrix.VrpProblemMatrix
import io.github.pintowar.opta.router.core.domain.ports.GeoPort
import org.springframework.context.annotation.Profile
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * The Controller that contains all REST functions to be used on the application.
 */
@RestController
@Profile(ConfigData.GEO_SERVER_PROFILE)
@RequestMapping("/api/geo")
class GeoController(private val geoPort: GeoPort, val objectMapper: ObjectMapper) {

    data class SimplePathRequest(val origin: LatLng, val target: LatLng)

    data class LocationsRequest(val depots: List<Depot>, val customers: List<Customer>)

    data class DetailedPathRequest(val routes: List<Route>)

    @PostMapping("/simple-path", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun simplePath(@RequestBody bounds: SimplePathRequest): Path =
        geoPort.simplePath(bounds.origin, bounds.target)

    @PostMapping("/generate-matrix", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun generateMatrix(@RequestBody locations: LocationsRequest): VrpProblemMatrix =
        geoPort.generateMatrix((locations.depots + locations.customers).toSet())

    @PostMapping("/detailed-paths", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun detailedPaths(@RequestBody plan: DetailedPathRequest): List<Route> =
        geoPort.detailedPaths(plan.routes)
}