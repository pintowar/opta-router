package io.github.pintowar.opta.router.core.domain.ports.repo

import io.github.pintowar.opta.router.core.domain.models.VrpSolverRequest
import kotlinx.coroutines.flow.Flow
import java.time.Duration
import java.util.*

/**
 * The VrpSolverRequestPort is responsible for managing the persistence of [VrpSolverRequest] data.
 */
interface VrpSolverRequestPort {
    /**
     * Refreshes the status of solver requests by setting "CREATED" requests that have exceeded a timeout to "TERMINATED".
     *
     * This function identifies solver requests that are currently in the "CREATED" state but have not been updated
     * within the specified timeout duration. These requests are then marked as "TERMINATED", indicating that
     * they are no longer actively being processed.
     *
     * @param timeout The [Duration] after which a "RUNNING" request is considered timed out and will be terminated.
     * @return The number of solver requests that were updated to "TERMINATED".
     */
    suspend fun refreshCreatedSolverRequests(timeout: Duration): Int

    /**
     * Refreshes the status of solver requests by setting "RUNNING" requests that have exceeded a timeout to "TERMINATED".
     *
     * This function identifies solver requests that are currently in the "RUNNING" state but have not been updated
     * within the specified timeout duration. These requests are then marked as "TERMINATED", indicating that
     * they are no longer actively being processed.
     *
     * @param timeout The [Duration] after which a "RUNNING" request is considered timed out and will be terminated.
     * @return The number of solver requests that were updated to "TERMINATED".
     */
    suspend fun refreshRunningSolverRequests(timeout: Duration): Int

    /**
     * Creates a new VRP solver request in the database.
     *
     * This function first checks if there are any existing enqueued or running solver requests
     * for the given problem ID. If there are, it returns `null` to prevent duplicate active requests.
     * Otherwise, it inserts the new solver request into the database with the current timestamp.
     *
     * @param request The [VrpSolverRequest] object to be created.
     * @return The created [VrpSolverRequest] object if successful, or `null` if an active request already exists for the problem.
     */
    suspend fun createRequest(request: VrpSolverRequest): VrpSolverRequest?

    /**
     * Update an existing VRP solver request status in the database.
     *
     * This function changes a solver request status from CREATED to ENQUEUED of a provided solverKey.
     *
     * @param solverKey The unique [UUID] key of the solver request.
     */
    suspend fun enqueueRequest(solverKey: UUID)

    /**
     * Retrieves the current (most recently updated) solver request for a given VRP problem ID.
     *
     * @param problemId The ID of the VRP problem for which to retrieve the solver request.
     * @return The [VrpSolverRequest] object if found, or `null` if no request exists for the problem.
     */
    suspend fun currentSolverRequest(problemId: Long): VrpSolverRequest?

    /**
     * Retrieves the current (most recently updated) solver request for a given solver key.
     *
     * @param solverKey The unique [UUID] key of the solver request.
     * @return The [VrpSolverRequest] object if found, or `null` if no request exists for the solver key.
     */
    suspend fun currentSolverRequest(solverKey: UUID): VrpSolverRequest?

    /**
     * Retrieves a flow of solver requests for a specific VRP problem ID and solver name.
     *
     * The requests are ordered by their creation timestamp.
     *
     * @param problemId The ID of the VRP problem.
     * @param solverName The name of the solver.
     * @return A [Flow] of [VrpSolverRequest] objects matching the criteria.
     */
    fun requestsByProblemIdAndSolverName(
        problemId: Long,
        solverName: String
    ): Flow<VrpSolverRequest>
}