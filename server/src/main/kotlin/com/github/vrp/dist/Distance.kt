package com.github.vrp.dist

import com.github.util.GraphWrapper

/**
 * Interface to calculate the distance and time between two points.
 */
interface Distance {
    fun distance(i: Int, j: Int): Double

    fun time(i: Int, j: Int): Double
}

/**
 * Calculates the distance of all points found in locations, based on the OSM map provided.
 *
 * @param locations list of points.
 * @param graph the wrapper used to calculate
 */
class PathDistance(private val locations: List<Pair<Double, Double>>, graph: GraphWrapper) : Distance {
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

/**
 * Calculates the distance of all points found in locations, based on a simple Euclidean distance.
 *
 * @param locations list of points.
 * @param avgSpeed average speed o calculate the time between points.
 */
class EuclideanDistance(private val locations: List<Pair<Double, Double>>, private val avgSpeed: Double = 60.0) : Distance {
    private val n = this.locations.size
    private val distMatrixSize = n * (n - 1) / 2
    private val distMatrix = createDistMatrix()

    private fun createDistMatrix(): DoubleArray {
        val matrix = DoubleArray(distMatrixSize)
        for (i in 0 until this.n) {
            for (j in i until this.n) {
                if (i != j) {
                    matrix[calcIdx(i, j)] = euclideanDistance(i, j)
                }
            }
        }
        return matrix
    }

    private fun calcIdx(i: Int, j: Int): Int {
        return distMatrixSize - (this.n - i) * (this.n - i - 1) / 2 + j - i - 1
    }

    private fun euclideanDistance(i: Int, j: Int): Double {
        val (firstX, firstY) = locations[i]
        val (secondX, secondY) = locations[j]
        return Math.sqrt(Math.pow(firstX - secondX, 2.0) + Math.pow(firstY - secondY, 2.0))
    }

    /**
     * This method returns the distance of the path.
     *
     * @return
     */
    override fun distance(i: Int, j: Int): Double {
        return if (i == j)
            0.0
        else if (i < j)
            distMatrix[calcIdx(i, j)]
        else
            distMatrix[calcIdx(j, i)]
    }

    /**
     * @return time
     */
    override fun time(i: Int, j: Int): Double {
        return this.distance(i, j) / this.avgSpeed
    }
}
