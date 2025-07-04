package io.github.pintowar.opta.router.core.domain.models.matrix

/**
 * Interface to calculate the distance and time between two points.
 */
interface Matrix {
    /**
     * Calculates the distance between two locations.
     *
     * @param originId The ID of the origin location.
     * @param targetId The ID of the target location.
     * @return The distance between the two locations.
     */
    fun distance(
        originId: Long,
        targetId: Long
    ): Double

    /**
     * Calculates the time between two locations.
     *
     * @param originId The ID of the origin location.
     * @param targetId The ID of the target location.
     * @return The time between the two locations.
     */
    fun time(
        originId: Long,
        targetId: Long
    ): Long
}