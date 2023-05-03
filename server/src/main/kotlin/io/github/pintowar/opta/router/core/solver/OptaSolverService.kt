package io.github.pintowar.opta.router.core.solver

import io.github.pintowar.opta.router.core.domain.models.Instance
import io.github.pintowar.opta.router.core.domain.models.SolverState
import io.github.pintowar.opta.router.core.domain.models.VrpSolution
import io.github.pintowar.opta.router.core.domain.models.VrpSolutionState
import io.github.pintowar.opta.router.core.domain.ports.BroadcastService
import io.github.pintowar.opta.router.core.domain.ports.GeoService
import io.github.pintowar.opta.router.core.domain.ports.SolverRepository
import io.github.pintowar.opta.router.core.domain.ports.VrpSolverService
import jakarta.annotation.PreDestroy
import mu.KotlinLogging
import org.optaplanner.core.api.solver.SolverManager
import org.optaplanner.core.api.solver.SolverStatus
import org.optaplanner.examples.vehiclerouting.domain.VehicleRoutingSolution
import org.springframework.stereotype.Service

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
    val graph: GeoService,
    val solverManager: SolverManager<VehicleRoutingSolution, Long>,
    val solverRepository: SolverRepository,
    val broadcastService: BroadcastService
) : VrpSolverService {

    private val notSolved = "not solved"
    private val running = "running"
    private val terminated = "terminated"

    /**
     * Terminates the solvers, in case of application termination.
     */
    @PreDestroy
    fun destroy() {
        solverRepository
            .listAllSolutionIds()
            .forEach(solverManager::terminateEarly)
    }

    fun wrapperForInstance(solution: VehicleRoutingSolution): VrpSolution {
        val current = solverRepository.currentSolutionState(solution.id)!!
        return solution.toDTO(current.solution.instance, graph, current.state.detailedPath)
    }

    fun broadcastSolution(instanceId: Long) {
        currentSolutionState(instanceId)
            ?.let(broadcastService::broadcastSolution)
    }

    override fun currentSolutionState(instanceId: Long): VrpSolutionState? =
        solverRepository.currentSolutionState(instanceId)

    override fun showState(instanceId: Long): SolverState =
        currentSolutionState(instanceId)?.state ?: SolverState(notSolved)

    override fun updateDetailedView(instanceId: Long, enabled: Boolean) {
        solverRepository.updateDetailedView(instanceId, enabled)?.also {
            solverRepository.updateSolution(
                it.solution.pathPlotted(graph, enabled),
                it.state.status
            )
            broadcastSolution(instanceId)
        }
    }

    override fun asyncSolve(instance: Instance) {
        if (solverManager.getSolverStatus(instance.id) == SolverStatus.NOT_SOLVING) {
            val currentSolution = solverRepository.currentSolutionState(instance.id)?.solution
                ?: solverRepository.createSolution(instance, SolverState(notSolved, false))
            val currentMatrix = solverRepository.currentMatrix(instance.id) ?: return

            val solution = currentSolution.toSolverSolution(currentMatrix)
            try {
                solverManager.solveAndListen(
                    solution.id,
                    { it: Long -> if (it == solution.id) solution else null },
                    { sol: VehicleRoutingSolution ->
                        solverRepository.updateSolution(wrapperForInstance(sol), running)
                        broadcastSolution(instance.id)
                    },
                    { sol: VehicleRoutingSolution ->
                        solverRepository.updateSolution(wrapperForInstance(sol), terminated)
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
        solverRepository.updateStatus(instance.id, running)
    }

    override fun terminateEarly(instanceId: Long): Boolean {
        return if (solverManager.getSolverStatus(instanceId) != SolverStatus.NOT_SOLVING) {
            solverManager.terminateEarly(instanceId)
            solverRepository.updateStatus(instanceId, terminated)
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
        val current = solverRepository.currentSolutionState(instanceId)?.solution
        if (current != null) {
            solverManager.terminateEarly(instanceId)
            solverRepository.updateStatus(instanceId, notSolved)
            solverRepository.updateDetailedView(instanceId, false)
            solverRepository.clearSolution(instanceId)
            broadcastSolution(instanceId)
        }
    }
}