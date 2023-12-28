package io.github.pintowar.opta.router.core.solver

import io.github.pintowar.opta.router.core.domain.models.SolverStatus
import io.github.pintowar.opta.router.core.domain.models.VrpSolutionRequest
import io.github.pintowar.opta.router.core.domain.ports.BroadcastPort
import io.github.pintowar.opta.router.core.domain.ports.SolverEventsPort
import io.github.pintowar.opta.router.core.domain.repository.SolverRepository
import io.github.pintowar.opta.router.core.solver.spi.Solver
import java.util.UUID

class VrpSolverService(
    private val solverEvents: SolverEventsPort,
    private val solverRepository: SolverRepository,
    private val broadcastPort: BroadcastPort
) {

    fun solverNames() = Solver.getNamedSolvers().keys

    suspend fun currentSolutionRequest(problemId: Long): VrpSolutionRequest? {
        return solverRepository.currentSolutionRequest(problemId)
    }

    suspend fun showStatus(problemId: Long): SolverStatus =
        solverRepository.currentSolverRequest(problemId)?.status ?: SolverStatus.NOT_SOLVED

    suspend fun showDetailedPath(problemId: Long) {
        solverRepository.currentSolutionRequest(problemId)?.let {
            broadcastPort.broadcastSolution(BroadcastPort.SolutionCommand(it))
        }
    }

    suspend fun update(solRequest: VrpSolutionRequest, clear: Boolean): VrpSolutionRequest {
        return solverRepository.addNewSolution(solRequest.solution, solRequest.solverKey!!, solRequest.status, clear)
    }

    suspend fun enqueueSolverRequest(problemId: Long, solverName: String): UUID? {
        return solverRepository.enqueue(problemId, solverName)?.let { request ->
            solverRepository.currentDetailedSolution(problemId)?.let { detailedSolution ->
                solverEvents.enqueueRequestSolver(
                    SolverEventsPort.RequestSolverCommand(
                        detailedSolution,
                        request.requestKey,
                        solverName
                    )
                )
                request.requestKey
            }
        }
    }

    suspend fun terminate(solverKey: UUID) = terminateEarly(solverKey, false)

    suspend fun clear(solverKey: UUID) = terminateEarly(solverKey, true)

    private suspend fun terminateEarly(solverKey: UUID, clear: Boolean) {
        solverRepository.currentSolverRequest(solverKey)?.also { solverRequest ->
            if (solverRequest.status in listOf(SolverStatus.RUNNING, SolverStatus.ENQUEUED)) {
                solverEvents.broadcastCancelSolver(
                    SolverEventsPort.CancelSolverCommand(solverKey, solverRequest.status, clear)
                )
            } else if (solverRequest.status == SolverStatus.TERMINATED && clear) {
                solverRepository.currentSolutionRequest(solverRequest.problemId)?.let { solutionRequest ->
                    solverEvents.enqueueSolutionRequest(SolverEventsPort.SolutionRequestCommand(solutionRequest, true))
                }
            }
        }
    }
}