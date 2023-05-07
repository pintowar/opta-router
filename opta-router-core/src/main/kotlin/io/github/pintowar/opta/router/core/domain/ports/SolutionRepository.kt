package io.github.pintowar.opta.router.core.domain.ports

import io.github.pintowar.opta.router.core.domain.models.VrpSolution
import io.github.pintowar.opta.router.core.domain.models.matrix.Matrix

interface SolutionRepository {

    fun listAll(): List<VrpSolution>

    fun getByInstanceId(instanceId: Long): VrpSolution?

    fun getByMatrixInstanceId(instanceId: Long): Matrix?
}