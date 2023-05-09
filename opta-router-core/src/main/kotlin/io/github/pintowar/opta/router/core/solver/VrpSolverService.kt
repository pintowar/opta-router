package io.github.pintowar.opta.router.core.solver

import io.github.pintowar.opta.router.core.domain.models.VrpProblem
import io.github.pintowar.opta.router.core.domain.models.SolverState
import io.github.pintowar.opta.router.core.domain.models.VrpSolutionRegistry

interface VrpSolverService {
    fun asyncSolve(instance: VrpProblem)

    fun showState(instanceId: Long): SolverState

    fun updateDetailedView(instanceId: Long, enabled: Boolean)

    fun terminateEarly(instanceId: Long): Boolean

    fun currentSolutionState(instanceId: Long): VrpSolutionRegistry?

    fun clean(instanceId: Long)
}