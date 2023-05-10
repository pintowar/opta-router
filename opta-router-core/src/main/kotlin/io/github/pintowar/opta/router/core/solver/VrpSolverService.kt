package io.github.pintowar.opta.router.core.solver

import io.github.pintowar.opta.router.core.domain.models.VrpProblem
import io.github.pintowar.opta.router.core.domain.models.SolverState
import io.github.pintowar.opta.router.core.domain.models.VrpSolutionRegistry

interface VrpSolverService {
    fun asyncSolve(instance: VrpProblem)

    fun showState(problemId: Long): SolverState

    fun updateDetailedView(problemId: Long, enabled: Boolean)

    fun terminateEarly(problemId: Long)

    fun currentSolutionState(problemId: Long): VrpSolutionRegistry?

    fun clean(problemId: Long)
}