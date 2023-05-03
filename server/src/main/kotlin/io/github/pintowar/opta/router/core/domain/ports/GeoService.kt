package io.github.pintowar.opta.router.core.domain.ports

import io.github.pintowar.opta.router.core.domain.models.Coordinate
import io.github.pintowar.opta.router.core.domain.models.Path

interface GeoService {

    fun simplePath(origin: Coordinate, target: Coordinate): Path

    fun detailedSimplePath(origin: Coordinate, target: Coordinate): Path
}