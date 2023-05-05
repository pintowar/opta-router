package io.github.pintowar.opta.router.adapters.database

import io.github.pintowar.opta.router.core.domain.models.Instance
import io.github.pintowar.opta.router.core.domain.models.SolverState
import io.github.pintowar.opta.router.core.domain.models.VrpSolution
import io.github.pintowar.opta.router.core.domain.models.VrpSolutionState
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

    private fun createSolution(instance: Instance): VrpSolutionState {
        val pu = PersistenceUnit(VrpSolution.emptyFromInstance(instance), SolverState.NOT_SOLVED)
        solutionIdMap[instance.id] = pu
        return VrpSolutionState(pu.vrpSolution, SolverState.NOT_SOLVED)
    }

    override fun updateSolution(sol: VrpSolution, solverState: SolverState) {
        val pu = solutionIdMap[sol.instance.id]
        if (pu != null) {
            solutionIdMap[sol.instance.id] = pu.copy(vrpSolution = sol, state = solverState)
        }
    }

    override fun updateStatus(instanceId: Long, solverState: SolverState) {
        val pu = solutionIdMap[instanceId]
        if (pu != null && pu.state != solverState) {
            solutionIdMap[instanceId] = pu.copy(state = solverState)
        }
    }

    override fun currentSolutionState(instanceId: Long): VrpSolutionState? {
        return solutionIdMap[instanceId]?.let {
            VrpSolutionState(it.vrpSolution, it.state)
        } ?: solutionRepository.getByInstanceId(instanceId)?.let { sol ->
            createSolution(sol.instance)
        }
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