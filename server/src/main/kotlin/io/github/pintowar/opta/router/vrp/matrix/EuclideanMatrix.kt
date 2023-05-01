package io.github.pintowar.opta.router.vrp.matrix

/**
 * Calculates the distance of all points found in locations, based on a simple Euclidean distance.
 *
 * @param locations list of points.
 * @param avgSpeed average speed o calculate the time between points.
 */
class EuclideanMatrix(
    private val locations: List<Pair<Double, Double>>,
    private val avgSpeed: Double = 60.0
) : Matrix {
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
        return if (i == j) {
            0.0
        } else if (i < j) {
            distMatrix[calcIdx(i, j)]
        } else {
            distMatrix[calcIdx(j, i)]
        }
    }

    /**
     * @return time
     */
    override fun time(i: Int, j: Int): Double {
        return this.distance(i, j) / this.avgSpeed
    }
}