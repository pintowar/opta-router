package com.github.service

import com.github.vrp.Instance
import com.github.vrp.SolverState
import com.github.vrp.VrpSolution
import com.github.vrp.dist.Distance
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