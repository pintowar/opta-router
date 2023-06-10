package io.github.pintowar.opta.router.core.domain.ports

import io.github.pintowar.opta.router.core.domain.models.VrpSolutionRequest
import java.util.*

interface SolverQueuePort {

    data class RequestSolverCommand(val problemId: Long, val uuid: UUID, val solverName: String)

    data class SolutionRequestCommand(val solutionRequest: VrpSolutionRequest, val clear: Boolean)

    fun requestSolver(command: RequestSolverCommand)

    fun updateAndBroadcast(command: SolutionRequestCommand)
}