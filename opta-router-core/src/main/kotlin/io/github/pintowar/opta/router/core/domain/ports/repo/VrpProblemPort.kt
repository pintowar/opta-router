package io.github.pintowar.opta.router.core.domain.ports.repo

import io.github.pintowar.opta.router.core.domain.models.VrpProblem
import io.github.pintowar.opta.router.core.domain.models.VrpProblemSummary
import io.github.pintowar.opta.router.core.domain.models.matrix.VrpProblemMatrix
import kotlinx.coroutines.flow.Flow

/**
 * The VrpProblemPort is responsible for managing the persistence of [VrpProblem] data.
 */
interface VrpProblemPort {
    /**
     * Finds all VRP problem summaries matching a given query, with pagination.
     *
     * This function retrieves a paginated list of VRP problem summaries, including counts
     * of solver requests by their status (enqueued, running, terminated, not solved).
     *
     * @param query The search query string to filter problems by name (case-insensitive).
     * @param offset The starting offset for pagination.
     * @param limit The maximum number of results to return.
     * @return A [Flow] of [VrpProblemSummary] objects that match the criteria.
     */
    fun findAll(
        query: String = "",
        offset: Int = 0,
        limit: Int = 25
    ): Flow<VrpProblemSummary>

    /**
     * Counts the number of VRP problems matching a given query.
     *
     * @param query The search query string to filter problems by name (case-insensitive).
     * @return The total number of VRP problems that match the criteria.
     */
    suspend fun count(query: String = ""): Long

    /**
     * Retrieves a VRP problem by its ID.
     *
     * @param problemId The ID of the VRP problem to retrieve.
     * @return The [VrpProblem] object if found, or `null` if not found.
     */
    suspend fun getById(problemId: Long): VrpProblem?

    /**
     * Creates a new VRP problem and its associated travel matrix in the database.
     *
     * This operation is performed within a transaction to ensure atomicity. It first generates
     * the travel matrix for the problem's locations and then inserts both the problem details
     * and the generated matrix into their respective tables.
     *
     * @param problem The [VrpProblem] object to be created.
     */
    suspend fun create(problem: VrpProblem)

    /**
     * Deletes a VRP problem by its ID.
     *
     * @param problemId The ID of the VRP problem to be deleted.
     */
    suspend fun deleteById(problemId: Long)

    /**
     * Updates an existing VRP problem and its associated travel matrix in the database.
     *
     * This operation is performed within a transaction to ensure atomicity. It first regenerates
     * the travel matrix for the problem's locations and then updates both the problem details
     * and the generated matrix in their respective tables.
     *
     * @param id The ID of the VRP problem to be updated.
     * @param problem The [VrpProblem] object containing the updated details.
     */
    suspend fun update(
        id: Long,
        problem: VrpProblem
    )

    /**
     * Retrieves the travel matrix for a given VRP problem ID.
     *
     * @param problemId The ID of the VRP problem for which to retrieve the matrix.
     * @return The [VrpProblemMatrix] object if found, or `null` if not found.
     */
    suspend fun getMatrixById(problemId: Long): VrpProblemMatrix?
}