package io.github.pintowar.opta.router.core.domain.ports

import io.github.pintowar.opta.router.core.domain.models.VrpProblem
import io.github.pintowar.opta.router.core.domain.models.matrix.VrpProblemMatrix
import kotlinx.coroutines.flow.Flow

interface VrpProblemPort {

    fun listAll(): Flow<VrpProblem>

    suspend fun getById(problemId: Long): VrpProblem?

    suspend fun deleteById(problemId: Long): Unit

    suspend fun getMatrixById(problemId: Long): VrpProblemMatrix?
}