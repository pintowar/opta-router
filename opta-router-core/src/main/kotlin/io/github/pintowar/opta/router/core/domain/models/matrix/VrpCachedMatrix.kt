package io.github.pintowar.opta.router.core.domain.models.matrix

import java.util.concurrent.ConcurrentHashMap

/**
 * A decorator for a [Matrix] that caches the results of distance and time calculations.
 * This can improve performance by avoiding repeated calculations for the same origin-target pairs.
 *
 * @param matrix The [Matrix] to decorate.
 */
class VrpCachedMatrix(
    private val matrix: Matrix
) : Matrix by matrix {
    private val distanceCache = ConcurrentHashMap<Pair<Long, Long>, Double>()
    private val timeCache = ConcurrentHashMap<Pair<Long, Long>, Long>()

    /**
     * Calculates the distance between two locations, using a cache to store and retrieve results.
     *
     * @param originId The ID of the origin location.
     * @param targetId The ID of the target location.
     * @return The distance between the two locations.
     */
    override fun distance(
        originId: Long,
        targetId: Long
    ): Double = distanceCache.computeIfAbsent(originId to targetId) { (i, j) -> matrix.distance(i, j) }

    /**
     * Calculates the time between two locations, using a cache to store and retrieve results.
     *
     * @param originId The ID of the origin location.
     * @param targetId The ID of the target location.
     * @return The time between the two locations.
     */
    override fun time(
        originId: Long,
        targetId: Long
    ): Long = timeCache.computeIfAbsent(originId to targetId) { (i, j) -> matrix.time(i, j) }
}