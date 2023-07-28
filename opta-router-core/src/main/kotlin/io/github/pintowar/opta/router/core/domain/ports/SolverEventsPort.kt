package io.github.pintowar.opta.router.core.domain.ports

import io.github.pintowar.opta.router.core.domain.models.VrpSolutionRequest
import java.util.*

interface SolverEventsPort {

    data class RequestSolverCommand(val problemId: Long, val solverKey: UUID, val solverName: String)

    data class CancelSolverCommand(val solverKey: UUID, val clear: Boolean = false)

    data class SolutionRequestCommand(val solutionRequest: VrpSolutionRequest, val clear: Boolean)

    fun enqueueRequestSolver(command: RequestSolverCommand)

    fun addRequestSolverListener(listener: (RequestSolverCommand) -> Unit)

    fun enqueueSolutionRequest(command: SolutionRequestCommand)

    fun addSolutionRequestListener(listener: (SolutionRequestCommand) -> Unit)

    fun broadcastCancelSolver(command: CancelSolverCommand)

    fun addBroadcastCancelListener(listener: (CancelSolverCommand) -> Unit)
}