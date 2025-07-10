package io.github.pintowar.opta.router.core.domain.models.matrix

/**
 * Interface to calculate the distance and time between two points.
 */
interface Matrix {
    fun distance(
        originId: Long,
        targetId: Long
    ): Double

    fun time(
        originId: Long,
        targetId: Long
    ): Long
}