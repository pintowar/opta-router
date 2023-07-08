package io.github.pintowar.opta.router.core.domain.ports

import io.github.pintowar.opta.router.core.domain.models.VrpSolverRequest
import java.time.Duration
import java.util.*

interface VrpSolverRequestPort {

    fun refreshSolverRequests(timeout: Duration)

    fun createRequest(request: VrpSolverRequest): VrpSolverRequest?

    fun currentSolverRequest(problemId: Long): VrpSolverRequest?

    fun currentSolverRequest(solverKey: UUID): VrpSolverRequest?

    fun requestsByProblemIdAndSolverName(problemId: Long, solverName: String): List<VrpSolverRequest>
}