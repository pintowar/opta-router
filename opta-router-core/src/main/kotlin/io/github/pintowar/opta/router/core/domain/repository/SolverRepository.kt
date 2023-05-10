package io.github.pintowar.opta.router.core.domain.repository

import io.github.pintowar.opta.router.core.domain.models.*
import io.github.pintowar.opta.router.core.domain.models.matrix.Matrix
import io.github.pintowar.opta.router.core.domain.ports.VrpProblemPort
import io.github.pintowar.opta.router.core.domain.ports.VrpSolverSolutionPort
import java.util.*

class SolverRepository(
    val vrpProblemPort: VrpProblemPort,
    val vrpSolverSolverSolutionPort: VrpSolverSolutionPort
) {

    fun clearSolution(problemId: Long) {
        vrpSolverSolverSolutionPort.clearSolution(problemId)
    }

    fun insertNewSolution(sol: VrpSolution, solverName: String, uuid: UUID, solverState: SolverState) {
        vrpSolverSolverSolutionPort.createNewSolution(sol.instance.id, solverName, solverState, sol.routes, uuid)
    }

    fun currentOrNewSolutionRegistry(problemId: Long, solverName: String): VrpSolutionRegistry? {
        return vrpProblemPort.getById(problemId)?.let { problem ->
            current(problem) ?: createNewSolutionFromInstance(problem, solverName)
        }
    }

    fun currentMatrix(instanceId: Long): Matrix? {
        return vrpProblemPort.getMatrixById(instanceId)
    }

    private fun current(problem: VrpProblem): VrpSolutionRegistry? {
        return vrpSolverSolverSolutionPort.currentSolution(problem.id)?.let { sol ->
            VrpSolutionRegistry(VrpSolution(problem, sol.routes), sol.state, sol.solverKey)
        }
    }

    private fun createNewSolutionFromInstance(
        problem: VrpProblem,
        solverName: String,
        solverState: SolverState = SolverState.NOT_SOLVED,
        paths: List<Route> = emptyList(),
        uuid: UUID? = null,
    ): VrpSolutionRegistry {
        vrpSolverSolverSolutionPort.createNewSolution(problem.id, solverName, solverState, paths, uuid)
        return VrpSolutionRegistry(VrpSolution(problem, paths), solverState, uuid)
    }

}