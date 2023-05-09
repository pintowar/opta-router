package io.github.pintowar.opta.router.core.domain.ports

import io.github.pintowar.opta.router.core.domain.models.VrpProblem
import io.github.pintowar.opta.router.core.domain.models.matrix.Matrix

interface VrpProblemRepository {

    fun listAll(): List<VrpProblem>

    fun getById(instanceId: Long): VrpProblem?

    fun getMatrixById(instanceId: Long): Matrix?
}