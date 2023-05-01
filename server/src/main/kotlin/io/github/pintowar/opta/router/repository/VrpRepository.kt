package io.github.pintowar.opta.router.repository

import io.github.pintowar.opta.router.vrp.Instance
import io.github.pintowar.opta.router.vrp.SolverState
import io.github.pintowar.opta.router.vrp.VrpSolution
import io.github.pintowar.opta.router.vrp.matrix.Matrix

interface VrpRepository {

    fun listAllSolutionIds(): Set<Long>

    fun createSolution(instance: Instance, solverState: SolverState)

    fun updateSolution(sol: VrpSolution, status: String, matrix: Matrix? = null)

    fun updateStatus(instanceId: Long, status: String)

    fun updateDetailedView(instanceId: Long, showDetailedView: Boolean)

    fun currentInstance(instanceId: Long): Instance?

    fun currentSolution(instanceId: Long): VrpSolution?

    fun currentDistance(instanceId: Long): Matrix?

    fun currentState(instanceId: Long): SolverState?

    fun createInstance(instanceId: Long): Instance?

    fun clearSolution(instanceId: Long)
}