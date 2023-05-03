package io.github.pintowar.opta.router.core.domain.models.matrix

class EmptyMatrix : Matrix {
    override fun distance(i: Int, j: Int): Double = 0.0

    override fun time(i: Int, j: Int): Double = 0.0
}