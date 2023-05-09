package io.github.pintowar.opta.router.core.domain.models.matrix

import io.github.pintowar.opta.router.core.domain.models.Location

data class VrpProblemMatrix(
    private val locationIds: LongArray,
    private val travelDistances: DoubleArray,
    private val travelTimes: LongArray
) : Matrix {

    constructor(locationIds: List<Long>, travelDistances: List<Double>, travelTimes: List<Long>) :
            this(locationIds.toLongArray(), travelDistances.toDoubleArray(), travelTimes.toLongArray())

    private val locationIdxs = locationIds.withIndex().associate { (idx, it) -> it to idx }
    private val n = locationIds.size

    init {
        assert(n * n == travelTimes.size && n * n == travelDistances.size) {
            "Travel Times/Distances must have the squared number of elements on locations"
        }
    }

    override fun distance(originId: Long, targetId: Long): Double {
        val (a, b) = locationIdxs[originId]!! to locationIdxs[targetId]!!
        return travelDistances[a * n + b]
    }

    override fun time(originId: Long, targetId: Long): Long {
        val (a, b) = locationIdxs[originId]!! to locationIdxs[targetId]!!
        return travelTimes[a * n + b]
    }
}