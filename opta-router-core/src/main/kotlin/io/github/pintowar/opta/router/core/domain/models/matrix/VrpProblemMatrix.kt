package io.github.pintowar.opta.router.core.domain.models.matrix

/**
 * Represents a pre-calculated matrix of distances and times between locations for a VRP problem.
 * This class provides an efficient way to look up travel times and distances between locations.
 *
 * @param locationIds An array of location IDs.
 * @param travelDistances A flattened 2D array representing the travel distances between locations.
 * @param travelTimes A flattened 2D array representing the travel times between locations.
 */
class VrpProblemMatrix(
    private val locationIds: LongArray,
    private val travelDistances: DoubleArray,
    private val travelTimes: LongArray
) : Matrix {
    /**
     * Secondary constructor that takes lists instead of arrays.
     *
     * @param locationIds A list of location IDs.
     * @param travelDistances A list of travel distances.
     * @param travelTimes A list of travel times.
     */
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

    /**
     * Calculates the index in the flattened 1D array corresponding to a 2D matrix index.
     *
     * @param i The row index.
     * @param j The column index.
     * @return The index in the 1D array.
     */
    private fun realIdx(
        i: Int,
        j: Int
    ): Int = i * n + j

    /**
     * Retrieves the distance between two locations from the pre-calculated matrix.
     *
     * @param originId The ID of the origin location.
     * @param targetId The ID of the target location.
     * @return The distance between the two locations.
     */
    override fun distance(
        originId: Long,
        targetId: Long
    ): Double {
        val (i, j) = locationIdxById.getValue(originId) to locationIdxById.getValue(targetId)
        return travelDistances[realIdx(i, j)]
    }

    /**
     * Retrieves the travel time between two locations from the pre-calculated matrix.
     *
     * @param originId The ID of the origin location.
     * @param targetId The ID of the target location.
     * @return The travel time between the two locations.
     */
    override fun time(
        originId: Long,
        targetId: Long
    ): Long {
        val (i, j) = locationIdxById.getValue(originId) to locationIdxById.getValue(targetId)
        return travelTimes[realIdx(i, j)]
    }

    /**
     * Returns the array of location IDs.
     *
     * @return An array of location IDs.
     */
    fun getLocationIds(): Array<Long?> = Array(locationIds.size) { locationIds.getOrNull(it) }

    /**
     * Returns the array of travel distances.
     *
     * @return An array of travel distances.
     */
    fun getTravelDistances(): Array<Double?> = Array(travelDistances.size) { travelDistances.getOrNull(it) }

    /**
     * Returns the array of travel times.
     *
     * @return An array of travel times.
     */
    fun getTravelTimes(): Array<Long?> = Array(travelTimes.size) { travelTimes.getOrNull(it) }
}