package io.github.pintowar.opta.router.core.solver

import io.github.pintowar.opta.router.core.domain.models.SolverStatus
import io.github.pintowar.opta.router.core.domain.models.VrpSolutionRequest
import io.github.pintowar.opta.router.core.domain.ports.BroadcastPort
import io.github.pintowar.opta.router.core.domain.ports.SolverEventsPort
import io.github.pintowar.opta.router.core.domain.repository.SolverRepository
import io.github.pintowar.opta.router.core.solver.spi.Solver
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import java.time.Duration
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class VrpSolverService(
    private val solverEvents: SolverEventsPort,
    private val solverRepository: SolverRepository,
    private val broadcastPort: BroadcastPort
) {

    init {
        solverEvents.addSolutionRequestListener { updateAndBroadcast(it.solutionRequest, it.clear) }
    }

    fun solverNames() = Solver.getNamedSolvers().keys

    fun currentSolutionRequest(problemId: Long): VrpSolutionRequest? {
        return solverRepository.currentSolutionRequest(problemId)
    }

    fun showStatus(problemId: Long): SolverStatus =
        solverRepository.currentSolverRequest(problemId)?.status ?: SolverStatus.NOT_SOLVED
//        solverRepository.refreshAndGetCurrentSolverRequest(problemId, timeLimit)?.status ?: SolverStatus.NOT_SOLVED

    fun updateDetailedView(problemId: Long) {
        solverRepository.currentSolutionRequest(problemId)?.let(broadcastPort::broadcastSolution)
    }

    fun enqueueSolverRequest(problemId: Long, solverName: String): UUID? {
        return solverRepository.enqueue(problemId, solverName)?.let { request ->
            solverEvents.enqueueRequestSolver(
                SolverEventsPort.RequestSolverCommand(
                    request.problemId,
                    request.requestKey,
                    solverName
                )
            )
            request.requestKey
        }
    }

    fun terminate(solverKey: UUID): Unit = terminateEarly(solverKey, false)

    fun clear(solverKey: UUID): Unit = terminateEarly(solverKey, true)

    private fun terminateEarly(solverKey: UUID, clear: Boolean) {
        solverRepository.currentSolverRequest(solverKey)?.also { solverRequest ->
            // TODO: must act when is ENQUEUED
            if (solverRequest.status == SolverStatus.RUNNING) {
                solverEvents.broadcastCancelSolver(SolverEventsPort.CancelSolverCommand(solverKey, clear))
            } else if (solverRequest.status == SolverStatus.TERMINATED && clear) {
                solverRepository.currentSolutionRequest(solverRequest.problemId)?.let { solutionRequest ->
                    solverEvents.enqueueSolutionRequest(SolverEventsPort.SolutionRequestCommand(solutionRequest, true))
                }
            }
        }
    }

    private fun updateAndBroadcast(solRequest: VrpSolutionRequest, clear: Boolean) {
        val newSolRequest = solverRepository
            .addNewSolution(solRequest.solution, solRequest.solverKey!!, solRequest.status, clear)
        broadcastPort.broadcastSolution(newSolRequest)
    }

}