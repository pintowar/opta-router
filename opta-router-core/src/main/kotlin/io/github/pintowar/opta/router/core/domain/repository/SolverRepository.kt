package io.github.pintowar.opta.router.core.domain.repository

import io.github.pintowar.opta.router.core.domain.models.*
import io.github.pintowar.opta.router.core.domain.models.matrix.Matrix
import io.github.pintowar.opta.router.core.domain.ports.VrpProblemPort
import io.github.pintowar.opta.router.core.domain.ports.VrpSolverRequestPort
import io.github.pintowar.opta.router.core.domain.ports.VrpSolverSolutionPort
import java.util.*

class SolverRepository(
    val vrpProblemPort: VrpProblemPort,
    val vrpSolverSolverSolutionPort: VrpSolverSolutionPort,
    val vrpSolverRequestPort: VrpSolverRequestPort
) {

    fun enqueue(problemId: Long, solverName: String): VrpSolverRequest? {
        return vrpSolverRequestPort.createRequest(
            VrpSolverRequest(UUID.randomUUID(), problemId, solverName, SolverStatus.ENQUEUED)
        )
    }

    fun currentSolverStatus(problemId: Long): VrpSolverRequest? {
        return vrpSolverRequestPort.currentSolverStatus(problemId)
    }

    fun clearSolution(problemId: Long) {
        vrpSolverSolverSolutionPort.clearSolution(problemId)
    }

    fun insertNewSolution(sol: VrpSolution, uuid: UUID, solverStatus: SolverStatus) {
        vrpSolverRequestPort.updateSolverStatus(uuid, solverStatus)
        vrpSolverSolverSolutionPort.createNewSolution(sol.problem.id, solverStatus, sol.routes, uuid)
    }

    fun latestOrNewSolutionRegistry(problemId: Long, uuid: UUID): VrpSolutionRegistry? {
        return vrpProblemPort.getById(problemId)?.let { problem ->
            latest(problem)?.copy(solverKey = uuid) ?: createNewSolutionFromInstance(problem, uuid)
        }
    }

    fun latestSolution(problemId: Long): VrpSolutionRegistry? {
        return vrpProblemPort.getById(problemId)?.let { problem ->
            latest(problem)
        }
    }

    fun currentMatrix(instanceId: Long): Matrix? {
        return vrpProblemPort.getMatrixById(instanceId)
    }

    private fun latest(problem: VrpProblem): VrpSolutionRegistry? {
        return vrpSolverSolverSolutionPort.currentSolution(problem.id)?.let { sol ->
            VrpSolutionRegistry(VrpSolution(problem, sol.routes), sol.status, sol.solverKey)
        }
    }

    private fun createNewSolutionFromInstance(
        problem: VrpProblem,
        uuid: UUID,
        solverStatus: SolverStatus = SolverStatus.NOT_SOLVED,
        paths: List<Route> = emptyList(),
    ): VrpSolutionRegistry {
        vrpSolverSolverSolutionPort.createNewSolution(problem.id, solverStatus, paths, uuid)
        return VrpSolutionRegistry(VrpSolution(problem, paths), solverStatus, uuid)
    }
}