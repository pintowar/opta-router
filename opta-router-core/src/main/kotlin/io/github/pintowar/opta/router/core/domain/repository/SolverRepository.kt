package io.github.pintowar.opta.router.core.domain.repository

import io.github.pintowar.opta.router.core.domain.models.SolverStatus
import io.github.pintowar.opta.router.core.domain.models.VrpDetailedSolution
import io.github.pintowar.opta.router.core.domain.models.VrpSolution
import io.github.pintowar.opta.router.core.domain.models.VrpSolutionRequest
import io.github.pintowar.opta.router.core.domain.models.VrpSolverRequest
import io.github.pintowar.opta.router.core.domain.ports.repo.VrpProblemPort
import io.github.pintowar.opta.router.core.domain.ports.repo.VrpSolverRequestPort
import io.github.pintowar.opta.router.core.domain.ports.repo.VrpSolverSolutionPort
import java.util.UUID

/**
 * The SolverRepository is responsible for managing the persistence of solver-related data, including problems,
 * solutions, and requests. It acts as a facade over the underlying data ports.
 *
 * @param vrpProblemPort The port for accessing VRP problem data.
 * @param vrpSolverSolutionPort The port for accessing VRP solver solution data.
 * @param vrpSolverRequestPort The port for accessing VRP solver request data.
 */
class SolverRepository(
    private val vrpProblemPort: VrpProblemPort,
    private val vrpSolverSolutionPort: VrpSolverSolutionPort,
    private val vrpSolverRequestPort: VrpSolverRequestPort
) {
    /**
     * Enqueues a new solver request for a given problem.
     *
     * @param problemId The ID of the problem to solve.
     * @param solverName The name of the solver to use.
     * @return The created [VrpSolverRequest], or null if the creation failed.
     */
    suspend fun enqueue(
        problemId: Long,
        solverName: String
    ): VrpSolverRequest? =
        vrpSolverRequestPort.createRequest(
            VrpSolverRequest(UUID.randomUUID(), problemId, solverName, SolverStatus.ENQUEUED)
        )

    /**
     * Retrieves the current solver request for a given problem.
     *
     * @param problemId The ID of the problem.
     * @return The current [VrpSolverRequest] if it exists, otherwise null.
     */
    suspend fun currentSolverRequest(problemId: Long): VrpSolverRequest? =
        vrpSolverRequestPort.currentSolverRequest(problemId)

    /**
     * Retrieves the current solver request for a given request key.
     *
     * @param requestKey The UUID of the solver request.
     * @return The current [VrpSolverRequest] if it exists, otherwise null.
     */
    suspend fun currentSolverRequest(requestKey: UUID): VrpSolverRequest? =
        vrpSolverRequestPort.currentSolverRequest(requestKey)

    /**
     * Retrieves the current solution request for a given problem.
     *
     * @param problemId The ID of the problem.
     * @return The current [VrpSolutionRequest] if it exists, otherwise null.
     */
    suspend fun currentSolutionRequest(problemId: Long): VrpSolutionRequest? =
        vrpSolverSolutionPort.currentSolutionRequest(problemId)

    /**
     * Adds a new solution to the repository.
     *
     * @param sol The [VrpSolution] to add.
     * @param uuid The UUID of the solver request.
     * @param solverStatus The status of the solver.
     * @param clear A boolean indicating whether to clear the existing solution (set paths to empty and status to NOT_SOLVED).
     * @return The updated [VrpSolutionRequest].
     */
    suspend fun addNewSolution(
        sol: VrpSolution,
        uuid: UUID,
        solverStatus: SolverStatus,
        clear: Boolean
    ): VrpSolutionRequest =
        vrpSolverSolutionPort.upsertSolution(
            sol.problem.id,
            solverStatus,
            sol.routes,
            sol.getTotalDistance().toDouble(),
            clear,
            uuid
        )

    /**
     * Retrieves the current detailed solution for a given problem.
     *
     * @param problemId The ID of the problem.
     * @return The [VrpDetailedSolution] if it exists, otherwise null.
     */
    suspend fun currentDetailedSolution(problemId: Long): VrpDetailedSolution? =
        vrpProblemPort.getMatrixById(problemId)?.let { currentMatrix ->
            vrpSolverSolutionPort.currentSolutionRequest(problemId)?.let { solutionRequest ->
                VrpDetailedSolution(solutionRequest.solution, currentMatrix)
            }
        }
}