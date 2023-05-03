package io.github.pintowar.opta.router.adapters.database

import io.github.pintowar.opta.router.core.domain.models.Instance
import io.github.pintowar.opta.router.core.domain.models.SolverState
import io.github.pintowar.opta.router.core.domain.models.VrpSolution
import io.github.pintowar.opta.router.core.domain.models.matrix.Matrix
import io.github.pintowar.opta.router.core.domain.ports.SolutionRepository
import io.github.pintowar.opta.router.core.domain.ports.SolverRepository
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

@Component
class SolverDummyRepository(
    private val solutionRepository: SolutionRepository
) : SolverRepository {

    private val solutionIdMap: ConcurrentMap<Long, PersistenceUnit> = ConcurrentHashMap()

    override fun listAllSolutionIds(): Set<Long> = solutionIdMap.keys

    override fun createSolution(instance: Instance, solverState: SolverState): VrpSolution {
        return PersistenceUnit(VrpSolution.emptyFromInstance(instance), solverState).also {
            solutionIdMap[instance.id] = it
        }.vrpSolution

    }

    override fun updateSolution(sol: VrpSolution, status: String) {
        val pu = solutionIdMap[sol.instance.id]
        if (pu != null) {
            val newState = pu.state.copy(status = status)
            solutionIdMap[sol.instance.id] = pu.copy(vrpSolution = sol, state = newState)
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

    override fun currentState(instanceId: Long): SolverState? {
        return solutionIdMap[instanceId]?.state
    }

    override fun currentMatrix(instanceId: Long): Matrix? {
        return solutionRepository.getByMatrixInstanceId(instanceId)
    }

    override fun clearSolution(instanceId: Long) {
        val pu = solutionIdMap[instanceId]
        if (pu != null) {
            solutionIdMap[instanceId] = pu.copy(vrpSolution = VrpSolution.emptyFromInstance(pu.vrpSolution.instance))
        }
    }
}

private data class PersistenceUnit(
    val vrpSolution: VrpSolution,
    val state: SolverState
)