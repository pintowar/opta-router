package io.github.pintowar.opta.router.core.domain.ports

import io.github.pintowar.opta.router.core.domain.models.Instance
import io.github.pintowar.opta.router.core.domain.models.SolverState
import io.github.pintowar.opta.router.core.domain.models.VrpSolutionState

interface VrpSolverService {
    fun asyncSolve(instance: Instance)

    fun showState(id: Long): SolverState?

    fun updateDetailedView(id: Long, status: Boolean)

    fun terminateEarly(id: Long): Boolean

    fun currentSolutionState(id: Long): VrpSolutionState?

    fun clean(id: Long)
}