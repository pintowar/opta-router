package io.github.pintowar.opta.router.core.domain.models.matrix

import io.github.pintowar.opta.router.core.domain.models.Location
import io.github.pintowar.opta.router.core.domain.ports.GeoPort

/**
 * Calculates the distance of all points found in locations, based on the OSM map provided.
 *
 * @param locations list of points.
 * @param graph the wrapper used to calculate
 */
class GeoMatrix(private val locations: List<Location>, graph: GeoPort) : Matrix {
    private val locationIds: Map<Long, Int> = locations.mapIndexed { idx, it -> it.id to idx }.toMap()
    private val n = this.locations.size
    private var distMatrix: Array<DoubleArray> = Array(this.n) { DoubleArray(this.n) }
    private var timeMatrix: Array<LongArray> = Array(this.n) { LongArray(this.n) }

    init {
        createDistMatrix(graph)
    }

    private fun createDistMatrix(graph: GeoPort) {
        for (i in 0 until this.n) {
            val a = locations[i]
            for (j in 0 until this.n) {
                if (i != j) {
                    val b = locations[j]
                    val path = graph.simplePath(a, b)
                    this.distMatrix[i][j] = path.distance
                    this.timeMatrix[i][j] = path.time
                } else {
                    this.distMatrix[i][j] = 0.0
                    this.timeMatrix[i][j] = 0
                }
            }
        }
    }

    /**
     * This method returns the distance of the path.
     *
     * @return distance in meter
     */
    override fun distance(originId: Long, targetId: Long): Double {
        val (a, b) = locationIds[originId]!! to locationIds[targetId]!!
        return distMatrix[a][b]
    }

    /**
     * @return time in millis
     */
    override fun time(originId: Long, targetId: Long): Long {
        val (a, b) = locationIds[originId]!! to locationIds[targetId]!!
        return timeMatrix[a][b]
    }
}