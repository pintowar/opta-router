package io.github.pintowar.opta.router.core.domain.models.matrix

class VrpProblemMatrix(
    private val locationIds: LongArray,
    private val travelDistances: DoubleArray,
    private val travelTimes: LongArray
) : Matrix {
    constructor(locationIds: List<Long>, travelDistances: List<Double>, travelTimes: List<Long>) :
        this(locationIds.toLongArray(), travelDistances.toDoubleArray(), travelTimes.toLongArray())

    private val locationIdxById = locationIds.withIndex().associate { (idx, it) -> it to idx }
    private val n = locationIds.size

    init {
        if (!(n * n == travelTimes.size && n * n == travelDistances.size)) {
            throw IllegalArgumentException(
                "Travel Times/Distances must have the squared number of elements on locations"
            )
        }
    }

    private fun realIdx(
        i: Int,
        j: Int
    ): Int = i * n + j

    override fun distance(
        originId: Long,
        targetId: Long
    ): Double {
        val (i, j) = locationIdxById.getValue(originId) to locationIdxById.getValue(targetId)
        return travelDistances[realIdx(i, j)]
    }

    override fun time(
        originId: Long,
        targetId: Long
    ): Long {
        val (i, j) = locationIdxById.getValue(originId) to locationIdxById.getValue(targetId)
        return travelTimes[realIdx(i, j)]
    }

    fun getLocationIds(): Array<Long?> = Array(locationIds.size) { locationIds.getOrNull(it) }

    fun getTravelDistances(): Array<Double?> = Array(travelDistances.size) { travelDistances.getOrNull(it) }

    fun getTravelTimes(): Array<Long?> = Array(travelTimes.size) { travelTimes.getOrNull(it) }
}