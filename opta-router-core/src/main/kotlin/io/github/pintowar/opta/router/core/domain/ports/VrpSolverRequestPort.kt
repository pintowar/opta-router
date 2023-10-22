package io.github.pintowar.opta.router.core.domain.ports

import io.github.pintowar.opta.router.core.domain.models.VrpSolverRequest
import kotlinx.coroutines.flow.Flow
import java.time.Duration
import java.util.*

interface VrpSolverRequestPort {

    suspend fun refreshSolverRequests(timeout: Duration): Int

    suspend fun createRequest(request: VrpSolverRequest): VrpSolverRequest?

    suspend fun currentSolverRequest(problemId: Long): VrpSolverRequest?

    suspend fun currentSolverRequest(solverKey: UUID): VrpSolverRequest?

    fun requestsByProblemIdAndSolverName(problemId: Long, solverName: String): Flow<VrpSolverRequest>
}