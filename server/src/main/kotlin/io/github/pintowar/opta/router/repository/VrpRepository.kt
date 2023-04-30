package io.github.pintowar.opta.router.repository

import io.github.pintowar.opta.router.vrp.Instance
import io.github.pintowar.opta.router.vrp.SolverState
import io.github.pintowar.opta.router.vrp.VrpSolution
import io.github.pintowar.opta.router.vrp.dist.Distance
import org.optaplanner.examples.vehiclerouting.domain.VehicleRoutingSolution

interface VrpRepository {

    fun listAllSolutionIds(): Set<Long>

    fun createSolution(instance: Instance, solverState: SolverState)

    fun updateSolution(sol: VrpSolution, status: String, distance: Distance? = null)

    fun updateStatus(instanceId: Long, status: String)

    fun updateDetailedView(instanceId: Long, showDetailedView: Boolean)

    fun currentSolution(instanceId: Long): VrpSolution?

    fun currentSolverSolution(instanceId: Long): VehicleRoutingSolution?

    fun currentState(instanceId: Long): SolverState?

    fun createInstance(instanceId: Long): Instance?

    fun removeSolution(instanceId: Long)
}