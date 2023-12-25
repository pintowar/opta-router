package io.github.pintowar.opta.router.core.domain.ports

import io.github.pintowar.opta.router.core.domain.models.VrpProblem
import io.github.pintowar.opta.router.core.domain.models.VrpProblemSummary
import io.github.pintowar.opta.router.core.domain.models.matrix.VrpProblemMatrix
import kotlinx.coroutines.flow.Flow

interface VrpProblemPort {

    fun findAll(query: String = "", offset: Int = 0, limit: Int = 25): Flow<VrpProblemSummary>

    suspend fun count(query: String = ""): Long

    suspend fun getById(problemId: Long): VrpProblem?

    suspend fun deleteById(problemId: Long)

    suspend fun getMatrixById(problemId: Long): VrpProblemMatrix?
}