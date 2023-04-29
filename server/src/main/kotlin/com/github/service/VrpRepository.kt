package com.github.service

import com.github.vrp.Instance
import com.github.vrp.SolverState
import org.optaplanner.examples.vehiclerouting.domain.VehicleRoutingSolution

interface VrpRepository {

    fun listAllSolutionIds(): Set<Long>

    fun createSolution(instance: Instance, solution: VehicleRoutingSolution, solverState: SolverState)

    fun updateSolution(sol: VehicleRoutingSolution, status: String)

    fun updateStatus(instanceId: Long, status: String)

    fun updateDetailedView(instanceId: Long, showDetailedView: Boolean)

    fun currentSolution(instanceId: Long): VehicleRoutingSolution?

    fun currentState(instanceId: Long): SolverState?

    fun removeSolution(instanceId: Long)

}