package com.github.service.impl

import com.github.service.VrpRepository
import com.github.vrp.Instance
import com.github.vrp.SolverState
import com.github.vrp.VrpSolution
import com.github.vrp.dist.Distance
import com.github.vrp.dist.EmptyDistance
import org.optaplanner.examples.vehiclerouting.domain.VehicleRoutingSolution
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

@Component
class VrpRepositoryDummy : VrpRepository {

    private val solutionIdMap: ConcurrentMap<Long, PersistenceUnit> = ConcurrentHashMap()

    override fun listAllSolutionIds(): Set<Long> = solutionIdMap.keys

    override fun createSolution(instance: Instance, solverState: SolverState) {
        solutionIdMap[instance.id] =
            PersistenceUnit(instance, VrpSolution.emptyFromInstanceId(instance.id), solverState, EmptyDistance())
    }

    override fun updateSolution(sol: VrpSolution, status: String, distance: Distance?) {
        val pu = solutionIdMap[sol.instanceId]
        if (pu != null) {
            val newState = pu.state.copy(status = status)
            solutionIdMap[sol.instanceId] = pu.copy(vrpSolution = sol, state = newState).let {
                if (distance != null) it.copy(distance = distance) else it
            }
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

    override fun currentSolution(instanceId: Long): VrpSolution? {
        return solutionIdMap[instanceId]?.vrpSolution
    }

    override fun currentSolverSolution(instanceId: Long): VehicleRoutingSolution? {
        val instance = createInstance(instanceId)
        return if (instance != null) {
            val distance = currentDistance(instanceId)!!
            currentSolution(instanceId)!!.toSolverSolution(instance, distance)
        } else {
            null
        }
    }

    override fun currentState(instanceId: Long): SolverState? {
        return solutionIdMap[instanceId]?.state
    }

    override fun createInstance(instanceId: Long): Instance? {
        return solutionIdMap[instanceId]?.instance
    }

    override fun removeSolution(instanceId: Long) {
        solutionIdMap.remove(instanceId)
    }

    private fun currentDistance(instanceId: Long): Distance? {
        return solutionIdMap[instanceId]?.distance
    }
}

private data class PersistenceUnit(
    val instance: Instance,
    val vrpSolution: VrpSolution,
    val state: SolverState,
    val distance: Distance
)