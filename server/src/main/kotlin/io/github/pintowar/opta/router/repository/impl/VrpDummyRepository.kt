package io.github.pintowar.opta.router.repository.impl

import io.github.pintowar.opta.router.repository.VrpRepository
import io.github.pintowar.opta.router.vrp.Instance
import io.github.pintowar.opta.router.vrp.SolverState
import io.github.pintowar.opta.router.vrp.VrpSolution
import io.github.pintowar.opta.router.vrp.dist.Distance
import io.github.pintowar.opta.router.vrp.dist.EmptyDistance
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

@Component
class VrpDummyRepository : VrpRepository {

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

    override fun currentInstance(instanceId: Long): Instance? {
        return solutionIdMap[instanceId]?.instance
    }

    override fun currentSolution(instanceId: Long): VrpSolution? {
        return solutionIdMap[instanceId]?.vrpSolution
    }

    override fun currentDistance(instanceId: Long): Distance? {
        return solutionIdMap[instanceId]?.distance
    }

    override fun currentState(instanceId: Long): SolverState? {
        return solutionIdMap[instanceId]?.state
    }

    override fun createInstance(instanceId: Long): Instance? {
        return solutionIdMap[instanceId]?.instance
    }

    override fun clearSolution(instanceId: Long) {
        val pu = solutionIdMap[instanceId]
        if (pu != null) {
            solutionIdMap[instanceId] = pu.copy(vrpSolution = VrpSolution.emptyFromInstanceId(instanceId))
        }
    }
}

private data class PersistenceUnit(
    val instance: Instance,
    val vrpSolution: VrpSolution,
    val state: SolverState,
    val distance: Distance
)