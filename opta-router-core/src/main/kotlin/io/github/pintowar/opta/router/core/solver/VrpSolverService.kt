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

/**
 * The VrpSolverService is responsible for managing the solver process.
 * It handles requests to solve VRP problems, queries for the current solution, and manages the lifecycle of the solver.
 *
 * @param broadcastPort The port used to broadcast solution updates.
 * @param solverEventsPort The port used to send solver-related events.
 * @param solverRepository The repository for accessing solver-related data.
 */
class VrpSolverService(
    private val broadcastPort: BroadcastPort,
    private val solverEventsPort: SolverEventsPort,
    private val solverRepository: SolverRepository
) {
    /**
     * Retrieves the names of all available solvers.
     *
     * @return A set of strings, where each string is the name of a solver.
     */
    fun solverNames() = Solver.getNamedSolvers().keys

    /**
     * Retrieves the current solution request for a given problem.
     *
     * @param problemId The ID of the problem.
     * @return The current [VrpSolutionRequest] if it exists, otherwise null.
     */
    suspend fun currentSolutionRequest(problemId: Long): VrpSolutionRequest? =
        solverRepository.currentSolutionRequest(problemId)

    /**
     * Retrieves the current status of the solver for a given problem.
     *
     * @param problemId The ID of the problem.
     * @return The current [SolverStatus].
     */
    suspend fun showStatus(problemId: Long): SolverStatus =
        solverRepository.currentSolverRequest(problemId)?.status ?: SolverStatus.NOT_SOLVED

    /**
     * Broadcasts the detailed path of the current solution for a given problem.
     *
     * @param problemId The ID of the problem.
     */
    suspend fun showDetailedPath(problemId: Long) {
        solverRepository.currentSolutionRequest(problemId)?.let {
            broadcastPort.broadcastSolution(SolutionCommand(it))
        }
    }

    /**
     * Updates the solution request for a given problem.
     *
     * @param solRequest The [VrpSolutionRequest] to update.
     * @param clear A boolean indicating whether to clear the existing solution (set paths to empty and status to NOT_SOLVED).
     * @return The updated [VrpSolutionRequest].
     */
    suspend fun update(
        solRequest: VrpSolutionRequest,
        clear: Boolean
    ): VrpSolutionRequest =
        solverRepository.addNewSolution(solRequest.solution, solRequest.solverKey!!, solRequest.status, clear)

    /**
     * Enqueues a new solver request for a given problem.
     *
     * @param problemId The ID of the problem.
     * @param solverName The name of the solver to use.
     * @return The UUID of the enqueued request, or null if the request fails.
     */
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

    /**
     * Terminates a running solver process.
     *
     * @param solverKey The UUID of the solver to terminate.
     */
    suspend fun terminate(solverKey: UUID) = terminateEarly(solverKey, false)

    /**
     * Clears a terminated solver process and its associated data.
     *
     * @param solverKey The UUID of the solver to clear.
     */
    suspend fun clear(solverKey: UUID) = terminateEarly(solverKey, true)

    /**
     * Terminates or clears a solver process based on the clear flag.
     *
     * @param solverKey The UUID of the solver to terminate or clear.
     * @param clear A boolean flag indicating whether to clear the solver process.
     */
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