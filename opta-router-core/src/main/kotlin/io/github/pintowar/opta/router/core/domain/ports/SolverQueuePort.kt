package io.github.pintowar.opta.router.core.domain.ports

import io.github.pintowar.opta.router.core.domain.models.SolverStatus
import io.github.pintowar.opta.router.core.domain.models.VrpSolution
import io.github.pintowar.opta.router.core.domain.models.VrpSolutionRegistry
import java.util.*

interface SolverQueuePort {

    data class RequestSolverCommand(val problemId: Long, val uuid: UUID, val solverName: String)

    data class SolutionRegistryCommand(val solutionRegistry: VrpSolutionRegistry)

    fun requestSolver(command: RequestSolverCommand)

    fun updateAndBroadcast(command: SolutionRegistryCommand)
}