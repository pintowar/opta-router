package io.github.pintowar.opta.router.core.domain.ports

import io.github.pintowar.opta.router.core.domain.models.Route
import io.github.pintowar.opta.router.core.domain.models.SolverStatus
import io.github.pintowar.opta.router.core.domain.models.VrpSolutionRequest
import io.github.pintowar.opta.router.core.domain.models.VrpSolverObjective
import java.util.*

interface VrpSolverSolutionPort {

    fun currentSolution(problemId: Long): List<Route>

    fun currentSolutionRequest(problemId: Long): VrpSolutionRequest?

    fun upsertSolution(
        problemId: Long,
        solverStatus: SolverStatus,
        paths: List<Route>,
        objective: Double,
        clear: Boolean,
        uuid: UUID
    ): VrpSolutionRequest

    fun solutionHistory(problemId: Long, requestId: UUID): List<VrpSolverObjective>
}