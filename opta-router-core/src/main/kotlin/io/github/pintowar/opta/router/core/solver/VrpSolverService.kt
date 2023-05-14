package io.github.pintowar.opta.router.core.solver

import io.github.pintowar.opta.router.core.domain.models.SolverState
import io.github.pintowar.opta.router.core.domain.models.VrpProblem
import io.github.pintowar.opta.router.core.domain.models.VrpSolutionRegistry
import java.util.UUID

interface VrpSolverService {

    fun enqueueSolverRequest(problemId: Long): UUID?

    fun showState(problemId: Long): SolverState

    fun updateDetailedView(problemId: Long, enabled: Boolean)

    fun currentSolutionRegistry(problemId: Long): VrpSolutionRegistry?

    fun terminateEarly(solverKey: UUID)

    fun clean(solverKey: UUID)
}