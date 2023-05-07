package io.github.pintowar.opta.router.core.solver

import io.github.pintowar.opta.router.core.domain.models.RouteInstance
import io.github.pintowar.opta.router.core.domain.models.SolverState
import io.github.pintowar.opta.router.core.domain.models.VrpSolution
import io.github.pintowar.opta.router.core.domain.models.VrpSolutionRegistry
import io.github.pintowar.opta.router.core.domain.models.matrix.Matrix
import io.github.pintowar.opta.router.core.domain.ports.BroadcastService
import io.github.pintowar.opta.router.core.domain.ports.SolverRepository
import io.github.pintowar.opta.router.core.domain.ports.VrpSolverService
import jakarta.annotation.PreDestroy
import mu.KotlinLogging
import org.optaplanner.core.api.solver.SolverManager
import org.optaplanner.core.api.solver.SolverStatus
import org.optaplanner.examples.vehiclerouting.domain.VehicleRoutingSolution
import org.springframework.stereotype.Service
import java.util.*

private val logger = KotlinLogging.logger {}

/**
 * This service is responsible to solve VRP problems asynchronously, update the application status, solver and solutions
 * storages. It also sends notifications to the WebSocket queue of the proper user (just for the one that executed the
 * application).
 *
 * @param graph the graphhopper wrapper to help calculate distances between points.
 */
@Service
class OptaSolverService(
    val solverManager: SolverManager<VehicleRoutingSolution, Long>,
    val solverRepository: SolverRepository,
    val broadcastService: BroadcastService
) : VrpSolverService {

    /**
     * Terminates the solvers, in case of application termination.
     */
    @PreDestroy
    fun destroy() {
        solverRepository
            .listAllSolutionIds()
            .forEach(solverManager::terminateEarly)
    }

    fun broadcastSolution(instanceId: Long) {
        currentSolutionState(instanceId)
            ?.let(broadcastService::broadcastSolution)
    }

    override fun currentSolutionState(instanceId: Long): VrpSolutionRegistry? =
        solverRepository.currentOrNewSolutionRegistry(instanceId)

    override fun showState(instanceId: Long): SolverState =
        currentSolutionState(instanceId)?.state ?: SolverState.NOT_SOLVED

    override fun updateDetailedView(instanceId: Long, enabled: Boolean) {
        broadcastSolution(instanceId)
    }

    override fun asyncSolve(instance: RouteInstance) {
        if (solverManager.getSolverStatus(instance.id) == SolverStatus.NOT_SOLVING) {
            val currentSolutionRegistry = solverRepository.currentOrNewSolutionRegistry(instance.id)!!
            val currentMatrix = solverRepository.currentMatrix(instance.id) ?: return
            val solverKey = currentSolutionRegistry.solverKey ?: UUID.randomUUID()

            val solution = currentSolutionRegistry.solution.toSolverSolution(currentMatrix)
            solverRepository.addNewSolution(solution.toDTO(instance, currentMatrix), solverKey, SolverState.ENQUEUED)
            try {
                solverManager.solveAndListen(
                    solution.id,
                    { it: Long -> if (it == solution.id) solution else null },
                    { sol: VehicleRoutingSolution ->
                        solverRepository.addNewSolution(sol.toDTO(instance, currentMatrix), solverKey, SolverState.RUNNING)
                        broadcastSolution(instance.id)
                    },
                    { sol: VehicleRoutingSolution ->
                        solverRepository.addNewSolution(sol.toDTO(instance, currentMatrix), solverKey, SolverState.TERMINATED)
                        broadcastSolution(instance.id)
                    },
                    { it: Long, exp: Throwable ->
                        logger.warn(exp) { "Problem while solving problemId $it! ${exp.message}" }
                    }
                )
            } catch (e: IllegalStateException) {
                logger.warn(e) { "Instance ${instance.id} is already being solved." }
            }
        }
    }

    override fun terminateEarly(instanceId: Long): Boolean {
        return if (solverManager.getSolverStatus(instanceId) != SolverStatus.NOT_SOLVING) {
            solverManager.terminateEarly(instanceId)
//            solverRepository.updateStatus(instanceId, SolverState.TERMINATED)
            broadcastSolution(instanceId)
            true
        } else {
            false
        }
    }

    /**
     * Removes all information associated to the user (identified by sessionID).
     *
     */
    override fun clean(instanceId: Long) {
        val current = solverRepository.currentOrNewSolutionRegistry(instanceId)?.solution
        if (current != null) {
            solverManager.terminateEarly(instanceId)
//            solverRepository.updateStatus(instanceId, SolverState.NOT_SOLVED)
            solverRepository.clearSolution(instanceId)
            broadcastSolution(instanceId)
        }
    }
}