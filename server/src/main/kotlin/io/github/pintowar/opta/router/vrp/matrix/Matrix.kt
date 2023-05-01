package io.github.pintowar.opta.router.vrp.matrix

/**
 * Interface to calculate the distance and time between two points.
 */
interface Matrix {
    fun distance(i: Int, j: Int): Double

    fun time(i: Int, j: Int): Double
}