package io.github.pintowar.opta.router.core.domain.ports.service

import io.github.pintowar.opta.router.core.domain.models.Coordinate
import io.github.pintowar.opta.router.core.domain.models.Location
import io.github.pintowar.opta.router.core.domain.models.Path
import io.github.pintowar.opta.router.core.domain.models.Route
import io.github.pintowar.opta.router.core.domain.models.matrix.VrpProblemMatrix

/**
 * The GeoPort is responsible for providing geographical services, such as calculating paths and generating travel matrices.
 */
interface GeoPort {
    /**
     * Calculates a simple path between two coordinates.
     *
     * @param origin The starting coordinate.
     * @param target The destination coordinate.
     * @return A [Path] object representing the path between the two coordinates, including distance, time, and the origin/target coordinates.
     */
    suspend fun simplePath(
        origin: Coordinate,
        target: Coordinate
    ): Path

    /**
     * Calculates detailed paths for a list of routes.
     *
     * This function takes a list of [Route] objects, and for each route, it calculates
     * the detailed path by breaking it down into simple paths between consecutive coordinates.
     * It then aggregates the distances, times, and coordinates to form a new list of routes
     * with detailed path information.
     *
     * @param routes A list of [Route] objects for which detailed paths need to be calculated.
     * @return A [List] of [Route] objects, where each route contains detailed path coordinates,
     *         total distance, and total time.
     */
    suspend fun detailedPaths(routes: List<Route>): List<Route>

    /**
     * Generates a travel matrix (distances and times) for a given set of locations.
     *
     * This function calculates the travel distance and time between every pair of locations
     * in the provided set, creating a [VrpProblemMatrix] that can be used for VRP solving.
     *
     * @param locations A [Set] of [Location] objects for which the travel matrix needs to be generated.
     * @return A [VrpProblemMatrix] containing the IDs of the locations, and lists of travel distances and travel times.
     */
    suspend fun generateMatrix(locations: Set<Location>): VrpProblemMatrix
}