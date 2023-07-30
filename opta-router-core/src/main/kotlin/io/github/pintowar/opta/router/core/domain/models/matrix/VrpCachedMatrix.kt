package io.github.pintowar.opta.router.core.domain.models.matrix

import java.util.concurrent.ConcurrentHashMap

class VrpCachedMatrix(private val matrix: Matrix) : Matrix by matrix {

    private val distanceCache = ConcurrentHashMap<Pair<Long, Long>, Double>()
    private val timeCache = ConcurrentHashMap<Pair<Long, Long>, Long>()

    override fun distance(originId: Long, targetId: Long): Double {
        return distanceCache.computeIfAbsent(originId to targetId) { (i, j) -> matrix.distance(i, j) }
    }

    override fun time(originId: Long, targetId: Long): Long {
        return timeCache.computeIfAbsent(originId to targetId) { (i, j) -> matrix.time(i, j) }
    }
}