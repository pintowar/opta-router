package io.github.pintowar.opta.router.core.domain.ports

import io.github.pintowar.opta.router.core.domain.models.Coordinate
import io.github.pintowar.opta.router.core.domain.models.Location
import io.github.pintowar.opta.router.core.domain.models.Path
import io.github.pintowar.opta.router.core.domain.models.VrpSolution
import io.github.pintowar.opta.router.core.domain.models.matrix.VrpProblemMatrix

interface GeoPort {

    fun simplePath(origin: Coordinate, target: Coordinate): Path

    fun detailedPaths(solution: VrpSolution): VrpSolution

    fun generateMatrix(locations: Set<Location>): VrpProblemMatrix
}