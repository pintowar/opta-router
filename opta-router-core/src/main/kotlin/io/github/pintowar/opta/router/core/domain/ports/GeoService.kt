package io.github.pintowar.opta.router.core.domain.ports

import io.github.pintowar.opta.router.core.domain.models.Coordinate
import io.github.pintowar.opta.router.core.domain.models.Path
import io.github.pintowar.opta.router.core.domain.models.VrpSolution

interface GeoService {

    fun simplePath(origin: Coordinate, target: Coordinate): Path

    fun detailedPaths(solution: VrpSolution): VrpSolution
}