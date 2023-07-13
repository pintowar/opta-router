package io.github.pintowar.opta.router.core.domain.models.matrix

import java.util.concurrent.ConcurrentHashMap

class VrpProblemMatrix(
    private val locationIds: LongArray,
    private val travelDistances: DoubleArray,
    private val travelTimes: LongArray
) : Matrix {

    constructor(locationIds: List<Long>, travelDistances: List<Double>, travelTimes: List<Long>) :
        this(locationIds.toLongArray(), travelDistances.toDoubleArray(), travelTimes.toLongArray())

    private val locationIdxs = locationIds.withIndex().associate { (idx, it) -> it to idx }
    private val n = locationIds.size

    private val indexCache = ConcurrentHashMap<Pair<Int, Int>, Int>()

    init {
        assert(n * n == travelTimes.size && n * n == travelDistances.size) {
            "Travel Times/Distances must have the squared number of elements on locations"
        }
    }

    private fun cachedIndex(originId: Long, targetId: Long): Int {
        val (a, b) = locationIdxs[originId]!! to locationIdxs[targetId]!!
        return indexCache.computeIfAbsent(a to b) { (i, j) -> i * n + j }
    }

    override fun distance(originId: Long, targetId: Long): Double {
        return travelDistances[cachedIndex(originId, targetId)]
    }

    override fun time(originId: Long, targetId: Long): Long {
        return travelTimes[cachedIndex(originId, targetId)]
    }
}