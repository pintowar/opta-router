package io.github.pintowar.opta.router.core.domain.ports

import io.github.pintowar.opta.router.core.domain.models.VrpSolutionRequest
import java.util.*

interface SolverEventsPort {

    data class RequestSolverCommand(val problemId: Long, val uuid: UUID, val solverName: String)

    data class SolutionRequestCommand(val solutionRequest: VrpSolutionRequest, val clear: Boolean)

    fun enqueueRequestSolver(command: RequestSolverCommand)

    fun addRequestSolverListener(listener: (RequestSolverCommand) -> Unit)

    fun enqueueSolutionRequest(command: SolutionRequestCommand)

    fun addSolutionRequestListener(listener: (SolutionRequestCommand) -> Unit)
}