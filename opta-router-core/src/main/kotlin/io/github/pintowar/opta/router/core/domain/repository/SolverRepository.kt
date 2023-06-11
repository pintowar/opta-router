package io.github.pintowar.opta.router.core.domain.repository

import io.github.pintowar.opta.router.core.domain.models.SolverStatus
import io.github.pintowar.opta.router.core.domain.models.VrpSolution
import io.github.pintowar.opta.router.core.domain.models.VrpSolutionRequest
import io.github.pintowar.opta.router.core.domain.models.VrpSolverRequest
import io.github.pintowar.opta.router.core.domain.models.matrix.Matrix
import io.github.pintowar.opta.router.core.domain.ports.VrpProblemPort
import io.github.pintowar.opta.router.core.domain.ports.VrpSolverRequestPort
import io.github.pintowar.opta.router.core.domain.ports.VrpSolverSolutionPort
import java.util.*

class SolverRepository(
    private val vrpProblemPort: VrpProblemPort,
    private val vrpSolverSolutionPort: VrpSolverSolutionPort,
    private val vrpSolverRequestPort: VrpSolverRequestPort
) {

    fun enqueue(problemId: Long, solverName: String): VrpSolverRequest? {
        return vrpSolverRequestPort.createRequest(
            VrpSolverRequest(UUID.randomUUID(), problemId, solverName, SolverStatus.ENQUEUED)
        )
    }

    fun currentSolverRequest(problemId: Long): VrpSolverRequest? {
        return vrpSolverRequestPort.currentSolverRequest(problemId)
    }

    fun currentSolverRequest(requestKey: UUID): VrpSolverRequest? {
        return vrpSolverRequestPort.currentSolverRequest(requestKey)
    }

    fun currentSolutionRequest(problemId: Long): VrpSolutionRequest? {
        return vrpSolverSolutionPort.currentSolutionRequest(problemId)
    }

    fun insertNewSolution(sol: VrpSolution, uuid: UUID, solverStatus: SolverStatus, clear: Boolean): VrpSolutionRequest {
        return vrpSolverSolutionPort.upsertSolution(
            sol.problem.id,
            solverStatus,
            sol.routes,
            sol.getTotalDistance().toDouble(),
            clear,
            uuid
        )
    }

    fun currentMatrix(instanceId: Long): Matrix? {
        return vrpProblemPort.getMatrixById(instanceId)
    }

}