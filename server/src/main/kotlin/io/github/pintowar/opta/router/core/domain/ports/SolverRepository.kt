package io.github.pintowar.opta.router.core.domain.ports

import io.github.pintowar.opta.router.core.domain.models.SolverState
import io.github.pintowar.opta.router.core.domain.models.VrpSolution
import io.github.pintowar.opta.router.core.domain.models.VrpSolutionState
import io.github.pintowar.opta.router.core.domain.models.matrix.Matrix

interface SolverRepository {

    fun listAllSolutionIds(): Set<Long>

    fun updateSolution(sol: VrpSolution, solverState: SolverState)

    fun updateStatus(instanceId: Long, solverState: SolverState)

    fun currentSolutionState(instanceId: Long): VrpSolutionState?

    fun currentMatrix(instanceId: Long): Matrix?

    fun clearSolution(instanceId: Long)
}