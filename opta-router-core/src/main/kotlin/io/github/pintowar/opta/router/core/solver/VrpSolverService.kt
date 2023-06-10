package io.github.pintowar.opta.router.core.solver

import io.github.pintowar.opta.router.core.domain.models.SolverStatus
import io.github.pintowar.opta.router.core.domain.models.VrpSolutionRequest
import io.github.pintowar.opta.router.core.domain.ports.BroadcastPort
import io.github.pintowar.opta.router.core.domain.ports.SolverQueuePort
import io.github.pintowar.opta.router.core.domain.repository.SolverRepository
import io.github.pintowar.opta.router.core.solver.spi.Solver
import io.github.pintowar.opta.router.core.solver.spi.SolverFactory
import java.time.Duration
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * This service is responsible to solve VRP problems asynchronously, update the application status, solver and solutions
 * storages. It also sends notifications to the WebSocket queue of the proper user (just for the one that executed the
 * application).
 *
 */
class VrpSolverService(
    private val timeLimit: Duration,
    private val solverQueue: SolverQueuePort,
    private val solverRepository: SolverRepository,
    private val broadcastPort: BroadcastPort
) {

    private val solverKeys = ConcurrentHashMap<UUID, Solver>()

    fun currentSolutionRequest(problemId: Long): VrpSolutionRequest? {
        return solverRepository.currentSolutionRequest(problemId)
    }

    fun showStatus(problemId: Long): SolverStatus =
        solverRepository.currentSolverRequest(problemId)?.status ?: SolverStatus.NOT_SOLVED

    fun updateDetailedView(problemId: Long) {
        solverRepository.currentSolutionRequest(problemId)?.let(broadcastPort::broadcastSolution)
    }

    fun enqueueSolverRequest(problemId: Long, solverName: String): UUID? {
        return solverRepository.enqueue(problemId, solverName)?.let { request ->
            solverQueue.requestSolver(
                SolverQueuePort.RequestSolverCommand(
                    request.problemId,
                    request.requestKey,
                    solverName
                )
            )
            request.requestKey
        }
    }

    fun terminateEarly(solverKey: UUID) {
        val solverRequest = solverRepository.currentSolverRequest(solverKey)
        if (solverRequest?.status != SolverStatus.NOT_SOLVED) {
            solverKeys[solverKey]?.terminate()
        }
    }

    fun clean(solverKey: UUID) {
        val solverRequest = solverRepository.currentSolverRequest(solverKey)
        if (solverRequest != null) {
            solverKeys[solverKey]?.terminate()
            sequence<Boolean?> { solverKeys[solverKey]?.isSolving() }.takeWhile { it == true }
            solverRepository.currentSolutionRequest(solverRequest.problemId)?.let {
                broadcastSolution(it, true)
            }
        }
    }

    fun solve(problemId: Long, uuid: UUID, solverName: String) {
        if (solverKeys.containsKey(uuid)) return

        val currentMatrix = solverRepository.currentMatrix(problemId) ?: return
        val solutionRequest = solverRepository.currentSolutionRequest(problemId) ?: return
        val solverKey = solutionRequest.solverKey ?: return
        val currentSolution = solutionRequest.solution

        SolverFactory.createSolver(solverName, solverKey, SolverConfig(timeLimit)).also { solver ->
            solverKeys[solverKey] = solver
            solver.solve(currentSolution, currentMatrix, ::broadcastSolution)
        }
    }

    private fun broadcastSolution(solutionRequest: VrpSolutionRequest, clear: Boolean = false) {
        solverQueue.updateAndBroadcast(SolverQueuePort.SolutionRequestCommand(solutionRequest, clear))
    }
}