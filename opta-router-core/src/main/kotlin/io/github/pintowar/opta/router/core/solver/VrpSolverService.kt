package io.github.pintowar.opta.router.core.solver

import io.github.pintowar.opta.router.core.domain.messages.CancelSolverCommand
import io.github.pintowar.opta.router.core.domain.messages.RequestSolverCommand
import io.github.pintowar.opta.router.core.domain.messages.SolutionCommand
import io.github.pintowar.opta.router.core.domain.messages.SolutionRequestCommand
import io.github.pintowar.opta.router.core.domain.models.SolverStatus
import io.github.pintowar.opta.router.core.domain.models.VrpSolutionRequest
import io.github.pintowar.opta.router.core.domain.ports.events.BroadcastPort
import io.github.pintowar.opta.router.core.domain.ports.events.SolverEventsPort
import io.github.pintowar.opta.router.core.domain.repository.SolverRepository
import io.github.pintowar.opta.router.core.solver.spi.Solver
import java.util.UUID

class VrpSolverService(
    private val broadcastPort: BroadcastPort,
    private val solverEventsPort: SolverEventsPort,
    private val solverRepository: SolverRepository
) {
    fun solverNames() = Solver.getNamedSolvers().keys

    suspend fun currentSolutionRequest(problemId: Long): VrpSolutionRequest? =
        solverRepository.currentSolutionRequest(problemId)

    suspend fun showStatus(problemId: Long): SolverStatus =
        solverRepository.currentSolverRequest(problemId)?.status ?: SolverStatus.NOT_SOLVED

    suspend fun showDetailedPath(problemId: Long) {
        solverRepository.currentSolutionRequest(problemId)?.let {
            broadcastPort.broadcastSolution(SolutionCommand(it))
        }
    }

    suspend fun update(
        solRequest: VrpSolutionRequest,
        clear: Boolean
    ): VrpSolutionRequest =
        solverRepository.addNewSolution(solRequest.solution, solRequest.solverKey!!, solRequest.status, clear)

    suspend fun enqueueSolverRequest(
        problemId: Long,
        solverName: String
    ): UUID? {
        val request = solverRepository.enqueue(problemId, solverName)
        val detailedSolution = request?.let { solverRepository.currentDetailedSolution(problemId) } ?: return null
        val cmd = RequestSolverCommand(detailedSolution, request.requestKey, solverName)
        solverEventsPort.enqueueRequestSolver(cmd)
        return request.requestKey
    }

    suspend fun terminate(solverKey: UUID) = terminateEarly(solverKey, false)

    suspend fun clear(solverKey: UUID) = terminateEarly(solverKey, true)

    private suspend fun terminateEarly(
        solverKey: UUID,
        clear: Boolean
    ) {
        val solverRequest = solverRepository.currentSolverRequest(solverKey) ?: return

        if (solverRequest.status in listOf(SolverStatus.RUNNING, SolverStatus.ENQUEUED)) {
            solverEventsPort.broadcastCancelSolver(
                CancelSolverCommand(solverKey, solverRequest.status, clear)
            )
        } else if (solverRequest.status == SolverStatus.TERMINATED && clear) {
            val solutionRequest = solverRepository.currentSolutionRequest(solverRequest.problemId) ?: return
            solverEventsPort.enqueueSolutionRequest(SolutionRequestCommand(solutionRequest, true))
        }
    }
}