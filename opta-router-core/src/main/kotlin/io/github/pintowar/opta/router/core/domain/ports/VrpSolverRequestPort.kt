package io.github.pintowar.opta.router.core.domain.ports

import io.github.pintowar.opta.router.core.domain.models.SolverStatus
import io.github.pintowar.opta.router.core.domain.models.VrpSolverRequest
import java.util.*

interface VrpSolverRequestPort {

    fun createRequest(request: VrpSolverRequest): VrpSolverRequest?

    fun currentSolverRequest(problemId: Long): VrpSolverRequest?

    fun currentSolverRequest(solverKey: UUID): VrpSolverRequest?

    fun updateSolverStatus(solverKey: UUID, solverStatus: SolverStatus)
}