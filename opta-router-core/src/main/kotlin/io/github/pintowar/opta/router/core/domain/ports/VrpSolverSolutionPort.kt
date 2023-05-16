package io.github.pintowar.opta.router.core.domain.ports

import io.github.pintowar.opta.router.core.domain.models.Route
import io.github.pintowar.opta.router.core.domain.models.SolverStatus
import io.github.pintowar.opta.router.core.domain.models.VrpSolverSolution
import java.util.*

interface VrpSolverSolutionPort {

    fun currentSolution(problemId: Long): VrpSolverSolution?

    fun createNewSolution(
        instanceId: Long,
        solverStatus: SolverStatus = SolverStatus.NOT_SOLVED,
        paths: List<Route> = emptyList(),
        uuid: UUID? = null
    )

    fun clearSolution(problemId: Long)
}