package io.github.pintowar.opta.router.vrp.matrix

import io.github.pintowar.opta.router.util.GraphWrapper

/**
 * Calculates the distance of all points found in locations, based on the OSM map provided.
 *
 * @param locations list of points.
 * @param graph the wrapper used to calculate
 */
class PathMatrix(private val locations: List<Pair<Double, Double>>, graph: GraphWrapper) : Matrix {
    private val n = this.locations.size
    private var distMatrix: Array<DoubleArray>? = null
    private var timeMatrix: Array<LongArray>? = null

    init {
        createDistMatrix(graph)
    }

    private fun createDistMatrix(graph: GraphWrapper) {
        this.distMatrix = Array(this.n) { DoubleArray(this.n) }
        this.timeMatrix = Array(this.n) { LongArray(this.n) }
        for (i in 0 until this.n) {
            val a = locations[i]
            for (j in 0 until this.n) {
                if (i != j) {
                    val b = locations[j]
                    val path = graph.simplePath(a, b)
                    this.distMatrix!![i][j] = path.distance
                    this.timeMatrix!![i][j] = path.time
                } else {
                    this.distMatrix!![i][j] = 0.0
                    this.timeMatrix!![i][j] = 0
                }
            }
        }
    }

    /**
     * This method returns the distance of the path.
     *
     * @return distance in meter
     */
    override fun distance(i: Int, j: Int): Double {
        return distMatrix!![i][j]
    }

    /**
     * @return time in millis
     */
    override fun time(i: Int, j: Int): Double {
        return timeMatrix!![i][j].toDouble()
    }
}