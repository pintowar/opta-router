package io.github.pintowar.opta.router.service

import io.github.pintowar.opta.router.vrp.Coordinate
import io.github.pintowar.opta.router.vrp.Path

interface GeoService {

    fun simplePath(origin: Coordinate, target: Coordinate): Path

    fun detailedSimplePath(origin: Coordinate, target: Coordinate): Path
}