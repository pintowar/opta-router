package io.github.pintowar.opta.router.core.domain.ports

import io.github.pintowar.opta.router.core.domain.models.Coordinate
import io.github.pintowar.opta.router.core.domain.models.Location
import io.github.pintowar.opta.router.core.domain.models.Path
import io.github.pintowar.opta.router.core.domain.models.Route
import io.github.pintowar.opta.router.core.domain.models.matrix.VrpProblemMatrix

interface GeoPort {

    suspend fun simplePath(origin: Coordinate, target: Coordinate): Path

    suspend fun detailedPaths(routes: List<Route>): List<Route>

    suspend fun generateMatrix(locations: Set<Location>): VrpProblemMatrix
}