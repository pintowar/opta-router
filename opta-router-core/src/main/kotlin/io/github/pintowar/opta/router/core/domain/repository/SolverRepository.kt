package io.github.pintowar.opta.router.core.domain.repository

import io.github.pintowar.opta.router.core.domain.models.*
import io.github.pintowar.opta.router.core.domain.models.matrix.Matrix
import io.github.pintowar.opta.router.core.domain.ports.VrpProblemPort
import io.github.pintowar.opta.router.core.domain.ports.VrpSolverRequestPort
import io.github.pintowar.opta.router.core.domain.ports.VrpSolverSolutionPort
import java.util.*

class SolverRepository(
    val vrpProblemPort: VrpProblemPort,
    val vrpSolverSolutionPort: VrpSolverSolutionPort,
    val vrpSolverRequestPort: VrpSolverRequestPort
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
        //TODO single db request
        return latestSolution(problemId)?.let { solution ->
            currentSolverRequest(problemId)?.let { solverRequest ->
                VrpSolutionRequest(solution, solverRequest.status, solverRequest.requestKey)
            } ?: VrpSolutionRequest(solution, SolverStatus.NOT_SOLVED)
        }
    }

    fun clearSolution(problemId: Long) {
        vrpSolverSolutionPort.clearSolution(problemId)
    }

    fun insertNewSolution(sol: VrpSolution, uuid: UUID, solverStatus: SolverStatus, clear: Boolean) {
        //TODO single db request AND return solutionrequest
        vrpSolverRequestPort.updateSolverStatus(uuid, if (clear) SolverStatus.NOT_SOLVED else solverStatus)
        vrpSolverSolutionPort.upsertSolution(
            sol.problem.id, solverStatus, sol.routes, sol.getTotalDistance().toDouble(), clear, uuid
        )
    }

    fun currentMatrix(instanceId: Long): Matrix? {
        return vrpProblemPort.getMatrixById(instanceId)
    }

    private fun latestSolution(problemId: Long): VrpSolution? {
        return vrpProblemPort.getById(problemId)?.let(::latest)
    }

    private fun latest(problem: VrpProblem): VrpSolution {
        return VrpSolution(problem, vrpSolverSolutionPort.currentSolution(problem.id))
    }
}