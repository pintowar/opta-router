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

class SolverRepository(
    private val vrpProblemPort: VrpProblemPort,
    private val vrpSolverSolutionPort: VrpSolverSolutionPort,
    private val vrpSolverRequestPort: VrpSolverRequestPort
) {
    suspend fun enqueue(
        problemId: Long,
        solverName: String
    ): VrpSolverRequest? =
        vrpSolverRequestPort.createRequest(
            VrpSolverRequest(UUID.randomUUID(), problemId, solverName, SolverStatus.ENQUEUED)
        )

    suspend fun currentSolverRequest(problemId: Long): VrpSolverRequest? =
        vrpSolverRequestPort.currentSolverRequest(problemId)

    suspend fun currentSolverRequest(requestKey: UUID): VrpSolverRequest? =
        vrpSolverRequestPort.currentSolverRequest(requestKey)

    suspend fun currentSolutionRequest(problemId: Long): VrpSolutionRequest? =
        vrpSolverSolutionPort.currentSolutionRequest(problemId)

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

    suspend fun currentDetailedSolution(problemId: Long): VrpDetailedSolution? =
        vrpProblemPort.getMatrixById(problemId)?.let { currentMatrix ->
            vrpSolverSolutionPort.currentSolutionRequest(problemId)?.let { solutionRequest ->
                VrpDetailedSolution(solutionRequest.solution, currentMatrix)
            }
        }
}