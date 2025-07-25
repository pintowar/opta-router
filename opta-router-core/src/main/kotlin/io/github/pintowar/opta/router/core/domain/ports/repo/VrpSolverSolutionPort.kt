package io.github.pintowar.opta.router.core.domain.ports.repo

import io.github.pintowar.opta.router.core.domain.models.Route
import io.github.pintowar.opta.router.core.domain.models.SolverStatus
import io.github.pintowar.opta.router.core.domain.models.VrpSolutionRequest
import io.github.pintowar.opta.router.core.domain.models.VrpSolverObjective
import kotlinx.coroutines.flow.Flow
import java.util.*

/**
 * The VrpSolverSolutionPort is responsible for managing the persistence of VRP solver solutions.
 */
interface VrpSolverSolutionPort {
    /**
     * Retrieves the current solution (routes) for a given VRP problem ID.
     *
     * @param problemId The ID of the VRP problem.
     * @return A [List] of [Route] objects representing the current solution, or an empty list if no solution is found.
     */
    suspend fun currentSolution(problemId: Long): List<Route>

    /**
     * Retrieves the current VRP solution request for a given problem ID.
     *
     * This function fetches the most recent solution request associated with a specific VRP problem.
     *
     * @param problemId The ID of the VRP problem.
     * @return A [VrpSolutionRequest] object if a current solution request is found, or `null` otherwise.
     */
    suspend fun currentSolutionRequest(problemId: Long): VrpSolutionRequest?

    /**
     * Inserts or updates a VRP solution and its associated solver request in the database.
     *
     * This function performs a transactional operation to ensure data consistency. It updates the solver request
     * status, and either inserts a new solution or updates an existing one for the given problem. If `clear` is true,
     * the solution paths are set to empty and the solver status is set to `NOT_SOLVED`.
     *
     * @param problemId The ID of the VRP problem.
     * @param solverStatus The current status of the solver (e.g., ENQUEUED, RUNNING, TERMINATED, NOT_SOLVED).
     * @param paths A [List] of [Route] objects representing the solution paths.
     * @param objective The objective value of the solution.
     * @param clear A boolean indicating whether to clear the existing solution (set paths to empty and status to NOT_SOLVED).
     * @param uuid The unique [UUID] of the solver request.
     * @return The updated [VrpSolutionRequest] object.
     */
    suspend fun upsertSolution(
        problemId: Long,
        solverStatus: SolverStatus,
        paths: List<Route>,
        objective: Double,
        clear: Boolean,
        uuid: UUID
    ): VrpSolutionRequest

    /**
     * Retrieves the solution history (objectives) for a given VRP problem ID.
     *
     * This function returns a flow of [VrpSolverObjective] objects, representing the historical
     * objective values, solver details, and status for a specific VRP problem.
     *
     * @param problemId The ID of the VRP problem.
     * @return A [Flow] of [VrpSolverObjective] objects, ordered by creation time.
     */
    fun solutionHistory(problemId: Long): Flow<VrpSolverObjective>
}