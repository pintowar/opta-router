package io.github.pintowar.opta.router.core.domain.ports

import io.github.pintowar.opta.router.core.domain.models.VrpProblem
import io.github.pintowar.opta.router.core.domain.models.matrix.Matrix

interface VrpProblemPort {

    fun listAll(): List<VrpProblem>

    fun getById(problemId: Long): VrpProblem?

    fun getMatrixById(problemId: Long): Matrix?
}