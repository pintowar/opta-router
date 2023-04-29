package com.github.service

import com.github.util.GraphWrapper
import com.github.vrp.Instance
import com.github.vrp.SolverState
import com.github.vrp.convertSolution
import com.github.vrp.dist.PathDistance
import jakarta.annotation.PreDestroy
import mu.KLogging
import org.optaplanner.core.api.solver.SolverManager
import org.optaplanner.core.api.solver.SolverStatus
import org.optaplanner.examples.vehiclerouting.domain.VehicleRoutingSolution
import org.springframework.stereotype.Service

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
        val notificationService: NotificationService
) {

    companion object : KLogging()

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

    fun broadcastSolution(instanceId: Long) {
        vrpRepository.currentSolution(instanceId)?.let { solution ->
            val currentState = vrpRepository.currentState(instanceId)!!
            notificationService.broadcastSolution(
                    currentState,
                    solution.convertSolution(if (currentState.detailedPath) graph else null)
            )
        }
    }

    fun showStatus(instanceId: Long): String =
            if (solverManager.getSolverStatus(instanceId) == SolverStatus.NOT_SOLVING)
                vrpRepository.currentState(instanceId)?.status ?: SolverStatus.NOT_SOLVING.name
            else solverManager.getSolverStatus(instanceId).name

    fun updateDetailedView(instanceId: Long, enabled: Boolean) {
        vrpRepository.updateDetailedView(instanceId, enabled)
    }

    fun retrieveOrCreateSolution(instance: Instance): VehicleRoutingSolution {
        val current = vrpRepository.currentSolution(instance.id)
        if (current != null) return current

//            sendMessageToUser(sessionId, calculatingDistances)
        val points = instance.stops.map { it.toPair() }
        val pathDistance = PathDistance(points, graph)
        val solution = instance.toSolution(pathDistance)
        vrpRepository.createSolution(instance, solution, SolverState(distancesCalculated))
        broadcastSolution(instance.id)
        return solution

    }

    fun asyncSolve(instance: Instance) {
        if (solverManager.getSolverStatus(instance.id) == SolverStatus.NOT_SOLVING) {
            val solution = retrieveOrCreateSolution(instance)
            solverManager.solveAndListen(
                    solution.id,
                    { it: Long -> if (it == solution.id) solution else null },
                    { sol: VehicleRoutingSolution ->
                        vrpRepository.updateSolution(sol, running)
                        broadcastSolution(instance.id)
                    },
                    { sol: VehicleRoutingSolution ->
                        vrpRepository.updateSolution(sol, terminated)
                        broadcastSolution(instance.id)
                    },
                    { it: Long, exp: Throwable ->
                        logger.warn(exp) { "Problem while solving problemId $it! ${exp.message}" }
                    }
            )
        }

    }

    fun terminateEarly(instanceId: Long): Boolean {
        return if (solverManager.getSolverStatus(instanceId) != SolverStatus.NOT_SOLVING) {
            solverManager.terminateEarly(instanceId)
            vrpRepository.updateStatus(instanceId, terminated)
            broadcastSolution(instanceId)
            true
        } else false
    }

    /**
     * Removes all information associated to the user (identified by sessionID).
     *
     */
    fun clean(instanceId: Long) {
        val current = vrpRepository.currentSolution(instanceId)
        if (current != null) {
            solverManager.terminateEarly(instanceId)
            broadcastSolution(instanceId)
            vrpRepository.removeSolution(instanceId)
        }
    }

}
