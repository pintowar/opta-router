package io.github.pintowar.opta.router.core.domain.ports

import io.github.pintowar.opta.router.core.domain.models.RouteInstance
import io.github.pintowar.opta.router.core.domain.models.SolverState
import io.github.pintowar.opta.router.core.domain.models.VrpSolutionState

interface VrpSolverService {
    fun asyncSolve(instance: RouteInstance)

    fun showState(instanceId: Long): SolverState

    fun updateDetailedView(instanceId: Long, enabled: Boolean)

    fun terminateEarly(instanceId: Long): Boolean

    fun currentSolutionState(instanceId: Long): VrpSolutionState?

    fun clean(instanceId: Long)
}