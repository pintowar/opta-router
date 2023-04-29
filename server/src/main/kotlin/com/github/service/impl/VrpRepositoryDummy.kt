package com.github.service.impl

import com.github.service.VrpRepository
import com.github.vrp.Instance
import com.github.vrp.SolverState
import org.optaplanner.examples.vehiclerouting.domain.VehicleRoutingSolution
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

@Component
class VrpRepositoryDummy : VrpRepository {

    private val solutionIdMap: ConcurrentMap<Long, PersistenceUnit> = ConcurrentHashMap()

    override fun listAllSolutionIds(): Set<Long> = solutionIdMap.keys

    override fun createSolution(instance: Instance, solution: VehicleRoutingSolution, solverState: SolverState) {
        solutionIdMap[instance.id] = PersistenceUnit(instance, solution, solverState)
    }

    override fun updateSolution(sol: VehicleRoutingSolution, status: String) {
        val pu = solutionIdMap[sol.id]
        if (pu != null) {
            val newState = pu.state.copy(status = status)
            solutionIdMap[sol.id] = pu.copy(vehicleRoutingSolution = sol, state = newState)
        }
    }

    override fun updateStatus(instanceId: Long, status: String) {
        val pu = solutionIdMap[instanceId]
        if (pu != null && pu.state.status != status) {
            val newState = pu.state.copy(status = status)
            solutionIdMap[instanceId] = pu.copy(state = newState)
        }
    }

    override fun updateDetailedView(instanceId: Long, showDetailedView: Boolean) {
        val pu = solutionIdMap[instanceId]
        if (pu != null && pu.state.detailedPath != showDetailedView) {
            val newState = pu.state.copy(detailedPath = showDetailedView)
            solutionIdMap[instanceId] = pu.copy(state = newState)
        }
    }

    override fun currentSolution(instanceId: Long): VehicleRoutingSolution? {
        return solutionIdMap[instanceId]?.vehicleRoutingSolution
    }

    override fun currentState(instanceId: Long): SolverState? {
        return solutionIdMap[instanceId]?.state
    }

    override fun removeSolution(instanceId: Long) {
        solutionIdMap.remove(instanceId)
    }
}

private data class PersistenceUnit(
        val instance: Instance,
        val vehicleRoutingSolution: VehicleRoutingSolution,
        val state: SolverState
)