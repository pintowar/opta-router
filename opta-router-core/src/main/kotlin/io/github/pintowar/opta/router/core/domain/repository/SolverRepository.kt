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
            VrpSolverRequest(UUID.randomUUID(), problemId, solverName, SolverState.ENQUEUED)
        )
    }

    fun currentSolverStatus(problemId: Long): VrpSolverRequest? {
        return vrpSolverRequestPort.currentSolverStatus(problemId)
    }

    fun clearSolution(problemId: Long) {
        vrpSolverSolverSolutionPort.clearSolution(problemId)
    }

    fun insertNewSolution(sol: VrpSolution, uuid: UUID, solverState: SolverState) {
        vrpSolverRequestPort.updateSolverStatus(uuid, solverState)
        vrpSolverSolverSolutionPort.createNewSolution(sol.problem.id, solverState, sol.routes, uuid)
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
            VrpSolutionRegistry(VrpSolution(problem, sol.routes), sol.state, sol.solverKey)
        }
    }

    private fun createNewSolutionFromInstance(
        problem: VrpProblem,
        uuid: UUID,
        solverState: SolverState = SolverState.NOT_SOLVED,
        paths: List<Route> = emptyList(),
    ): VrpSolutionRegistry {
        vrpSolverSolverSolutionPort.createNewSolution(problem.id, solverState, paths, uuid)
        return VrpSolutionRegistry(VrpSolution(problem, paths), solverState, uuid)
    }
}