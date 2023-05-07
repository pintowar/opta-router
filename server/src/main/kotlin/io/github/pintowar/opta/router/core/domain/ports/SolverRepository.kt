package io.github.pintowar.opta.router.core.domain.ports

import io.github.pintowar.opta.router.core.domain.models.SolverState
import io.github.pintowar.opta.router.core.domain.models.VrpSolution
import io.github.pintowar.opta.router.core.domain.models.VrpSolutionRegistry
import io.github.pintowar.opta.router.core.domain.models.matrix.Matrix
import java.util.*

interface SolverRepository {

    fun listAllSolutionIds(): Set<Long>

    fun addNewSolution(sol: VrpSolution, uuid: UUID, solverState: SolverState)

    fun currentOrNewSolutionRegistry(instanceId: Long): VrpSolutionRegistry?

    fun currentMatrix(instanceId: Long): Matrix?

    fun clearSolution(instanceId: Long)
}