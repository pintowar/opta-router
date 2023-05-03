package io.github.pintowar.opta.router.core.domain.ports

import io.github.pintowar.opta.router.core.domain.models.Instance
import io.github.pintowar.opta.router.core.domain.models.SolverState
import io.github.pintowar.opta.router.core.domain.models.VrpSolution
import io.github.pintowar.opta.router.core.domain.models.VrpSolutionState
import io.github.pintowar.opta.router.core.domain.models.matrix.Matrix

interface SolverRepository {

    fun listAllSolutionIds(): Set<Long>

    fun createSolution(instance: Instance, solverState: SolverState): VrpSolution

    fun updateSolution(sol: VrpSolution, status: String)

    fun updateStatus(instanceId: Long, status: String)

    fun updateDetailedView(instanceId: Long, showDetailedView: Boolean): VrpSolutionState?

    fun currentSolutionState(instanceId: Long): VrpSolutionState?

    fun currentMatrix(instanceId: Long): Matrix?

    fun clearSolution(instanceId: Long)
}