package io.github.pintowar.opta.router.service

import io.github.pintowar.opta.router.repository.InstanceRepository
import io.github.pintowar.opta.router.repository.VrpRepository
import io.github.pintowar.opta.router.util.GraphWrapper
import io.github.pintowar.opta.router.vrp.Instance
import io.github.pintowar.opta.router.vrp.SolverState
import io.github.pintowar.opta.router.vrp.VrpSolution
import io.github.pintowar.opta.router.vrp.VrpSolutionState
import io.github.pintowar.opta.router.vrp.matrix.PathMatrix
import io.github.pintowar.opta.router.vrp.toDTO
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
class VrpSolverService(
    val graph: GraphWrapper,
    val solverManager: SolverManager<VehicleRoutingSolution, Long>,
    val vrpRepository: VrpRepository,
    val instanceRepository: InstanceRepository,
    val notificationService: NotificationService
) {

    private val notSolved = "not solved"
    private val calculatingDistances = "calculating distances"
    private val distancesCalculated = "distances calculated"
    private val running = "running"
    private val terminated = "terminated"

    /**
     * Terminates the solvers, in case of application termination.
     */
    @PreDestroy
    fun destroy() {
        vrpRepository
            .listAllSolutionIds()
            .forEach(solverManager::terminateEarly)
    }

    fun wrapperForInstance(solution: VehicleRoutingSolution): VrpSolution {
        val currentState = vrpRepository.currentState(solution.id)
        return solution.toDTO(if (currentState?.detailedPath == true) graph else null)
    }

    fun broadcastSolution(instanceId: Long) {
        currentSolutionState(instanceId)
            ?.let(notificationService::broadcastSolution)
    }

    fun retrieveOrCreateSolution(instance: Instance): VrpSolution {
        val current = vrpRepository.currentSolution(instance.id)
        return if (current != null) {
            current
        } else {
            vrpRepository.createSolution(instance, SolverState(notSolved))
            vrpRepository.currentSolution(instance.id)!!
        }
    }

    fun currentSolutionState(instanceId: Long): VrpSolutionState? {
        val instance = instanceRepository.getById(instanceId) ?: return null

        return retrieveOrCreateSolution(instance).let { solution ->
            val currentState = vrpRepository.currentState(instanceId)!!
            VrpSolutionState(solution, currentState)
        }
    }

    fun showState(instanceId: Long): SolverState? = vrpRepository.currentState(instanceId)

    fun updateDetailedView(instanceId: Long, enabled: Boolean) {
        vrpRepository.updateDetailedView(instanceId, enabled)

        val sol = vrpRepository.currentInstance(instanceId)?.let { instance ->
            toSolverSolution(instance, vrpRepository.currentSolution(instanceId)!!)
        }
        val currentStatus = vrpRepository.currentState(instanceId)?.status

        if (sol != null && currentStatus != null) {
            vrpRepository.updateSolution(wrapperForInstance(sol), currentStatus)
            broadcastSolution(instanceId)
        }
    }

    fun toSolverSolution(instance: Instance, solution: VrpSolution): VehicleRoutingSolution {
        val distance = vrpRepository.currentDistance(instance.id)!!
        return solution.toSolverSolution(instance, distance)
    }

    fun calculateDistanceMatrix(instance: Instance): VehicleRoutingSolution {
        val current = vrpRepository.currentSolution(instance.id)
        if (current != null && !current.isEmpty()) return toSolverSolution(instance, current)

        vrpRepository.updateSolution(current!!, calculatingDistances)
        broadcastSolution(instance.id)

        val points = instance.stops.map { it.toPair() }
        val pathDistance = PathMatrix(points, graph)
        val solution = instance.toSolution(pathDistance)

        vrpRepository.updateSolution(wrapperForInstance(solution), distancesCalculated, pathDistance)
        broadcastSolution(instance.id)
        return solution
    }

    fun asyncSolve(instance: Instance) {
        if (solverManager.getSolverStatus(instance.id) == SolverStatus.NOT_SOLVING) {
            val solution = calculateDistanceMatrix(instance)
            try {
                solverManager.solveAndListen(
                    solution.id,
                    { it: Long -> if (it == solution.id) solution else null },
                    { sol: VehicleRoutingSolution ->
                        vrpRepository.updateSolution(wrapperForInstance(sol), running)
                        broadcastSolution(instance.id)
                    },
                    { sol: VehicleRoutingSolution ->
                        vrpRepository.updateSolution(wrapperForInstance(sol), terminated)
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
        vrpRepository.updateStatus(instance.id, running)
    }

    fun terminateEarly(instanceId: Long): Boolean {
        return if (solverManager.getSolverStatus(instanceId) != SolverStatus.NOT_SOLVING) {
            solverManager.terminateEarly(instanceId)
            vrpRepository.updateStatus(instanceId, terminated)
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
    fun clean(instanceId: Long) {
        val current = vrpRepository.currentSolution(instanceId)
        if (current != null) {
            solverManager.terminateEarly(instanceId)
            vrpRepository.updateStatus(instanceId, notSolved)
            vrpRepository.updateDetailedView(instanceId, false)
            vrpRepository.clearSolution(instanceId)
            broadcastSolution(instanceId)
        }
    }
}