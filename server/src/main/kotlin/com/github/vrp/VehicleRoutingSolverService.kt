package com.github.opta

import com.github.util.GraphWrapper
import com.github.vrp.Instance
import com.github.vrp.VrpSolution
import com.github.vrp.convertSolution
import com.github.vrp.dist.PathDistance
import mu.KLogging
import org.optaplanner.core.api.solver.Solver
import org.optaplanner.core.api.solver.SolverFactory
import org.optaplanner.core.config.solver.termination.TerminationConfig
import org.optaplanner.examples.vehiclerouting.domain.VehicleRoutingSolution
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.messaging.simp.SimpMessageType
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.annotation.PreDestroy

/**
 * This service is responsible to solve VRP problems asynchronously, update the application status, solver and solutions
 * storages. It also sends notifications to the WebSocket queue of the proper user (just for the one that executed the
 * application).
 *
 * @param graph the graphhopper wrapper to help calculate distances between points.
 * @param sessionWebSocket the ConcurrentHashMap that associates the Socket Session ID to the Http Session ID.
 * @param messagingTemplate template to send messages to websocket queue.
 */
@Service
class VehicleRoutingSolverService(val graph: GraphWrapper, val sessionWebSocket: ConcurrentHashMap<String, String>, val messagingTemplate: SimpMessageSendingOperations) {

    companion object : KLogging() {
        private val SOLVER_CONFIG = "org/optaplanner/examples/vehiclerouting/solver/vehicleRoutingSolverConfig.xml"
    }

    private val solverFactory: SolverFactory<VehicleRoutingSolution> = SolverFactory.createFromXmlResource(SOLVER_CONFIG)

    private val sessionSolutionMap = HashMap<String, VehicleRoutingSolution>()
    private val sessionSolverMap = HashMap<String, Solver<VehicleRoutingSolution>>()
    private val sessionStatusMap = ConcurrentHashMap<String, String>()
    private val sessionDetailedView = ConcurrentHashMap<String, Boolean>()
    private val sessionInstance = ConcurrentHashMap<String, Instance>()

    private val calculatingDistances = "calculating distances"
    private val distancesCalculated = "distances calculated"
    private val running = "running"
    private val terminated = "terminated"

    init {
        // Always terminate a solver after 2 minutes
        val terminationConfig = TerminationConfig()
        terminationConfig.minutesSpentLimit = 2L
        solverFactory.solverConfig.terminationConfig = terminationConfig
    }

    /**
     * Terminates the solvers, in case of application termination.
     */
    @PreDestroy
    @Synchronized
    fun destroy() {
        for (solver in sessionSolverMap.values) {
            solver.terminateEarly()
        }
    }

    /**
     * Send a message (payload) to a specific user (identified by sessionID) on a specified destination (queue or topic).
     *
     * @param sessionId user Http Session ID.
     * @param destination queue/topic name.
     * @param payload message to send.
     */
    fun sendMessageToUser(sessionId: String, destination: String, payload: Any) {
        val headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE)
        val wsSession = sessionWebSocket[sessionId]!!
        headerAccessor.sessionId = wsSession
        headerAccessor.setLeaveMutable(true)
        messagingTemplate.convertAndSendToUser(wsSession, destination, payload, headerAccessor.messageHeaders)
    }

    /**
     * Change and notify the status of the solver.
     *
     * @param status the new status to inform.
     * @param sessionId user Http Session ID.
     */
    fun statusChange(status: String, sessionId: String) {
        sessionStatusMap[sessionId] = status
        sendMessageToUser(sessionId, "/queue/status", status)

    }

    /**
     * Update the solution storage and notify the solution queue if DTO solution is informed,
     * for the specific user (identified by sessionID).
     *
     * @param sessionId user Http Session ID.
     * @param bestSolution best solution so far.
     * @param sol DTO solution representation.
     */
    fun solutionChange(sessionId: String, bestSolution: VehicleRoutingSolution, sol: VrpSolution? = null) {
        if (terminated != sessionStatusMap[sessionId]) {
            sessionSolutionMap[sessionId] = bestSolution
            if (sol != null) sendMessageToUser(sessionId, "/queue/solution", sol)
        }
    }

    /**
     * Solver status of the specific user (identified by sessionID).
     *
     * @param sessionId user Http Session ID.
     */
    fun showStatus(sessionId: String): String = sessionStatusMap.getOrDefault(sessionId, "")

    /**
     * Store if "detailed view" mode is enabled or not for the specific user (identified by sessionID).
     *
     * @param sessionId user Http Session ID.
     * @param enabled view mode.
     */
    fun changeDetailedView(sessionId: String, enabled: Boolean) {
        sessionDetailedView[sessionId] = enabled
    }

    /**
     * Shows instance stored for the specific user (identified by sessionID).
     *
     * @param sessionId user Http Session ID.
     */
    fun currentInstance(sessionId: String) = sessionInstance[sessionId]

    /**
     * Shows if "detailed view" mode is enabled or not for the specific user (identified by sessionID).
     *
     * @param sessionId user Http Session ID.
     */
    fun isViewDetailed(sessionId: String) = sessionDetailedView.getOrDefault(sessionId, false)

    /**
     * Retrieve or create, update status and associates to a user (identified by sessionID) a
     * VRP Solution representation.
     *
     * @param sessionId user Http Session ID.
     * @param instance VRP problem instance.
     * @return solver's VRP Solution representation
     */
    @Synchronized
    fun retrieveOrCreateSolution(sessionId: String, instance: Instance? = null): VehicleRoutingSolution? {
        var solution: VehicleRoutingSolution? = sessionSolutionMap[sessionId]
        if (solution == null && instance != null) {
            statusChange(calculatingDistances, sessionId)
            val points = instance.stops.map { it.toPair() }
            val method = PathDistance(points, graph)
            solution = instance.toSolution(method)
            sessionInstance[sessionId] = instance
            sessionSolutionMap[sessionId] = solution
            statusChange(distancesCalculated, sessionId)
        }
        return solution
    }

    /**
     * Solves asynchronously the informed instance for the specific user (identified by sessionID).
     *
     * @param sessionId user Http Session ID.
     * @param instance VRP problem instance.
     */
    @Async
    fun solve(sessionId: String, instance: Instance) {
        retrieveOrCreateSolution(sessionId, instance)
        val solver = solverFactory.buildSolver()
        solver.addEventListener { event ->
            val bestSolution = event.newBestSolution
            val showDetailedPath = isViewDetailed(sessionId)
            logger.info("Detailed path: {}",  showDetailedPath)
            val sol = bestSolution.convertSolution(if (showDetailedPath) graph else null)
            logger.info("Best distance so far: {}",  sol.getTotalDistance())
            synchronized(this@VehicleRoutingSolverService) {
                solutionChange(sessionId, bestSolution, sol)
                statusChange(running, sessionId)
            }
        }

        if (!sessionSolverMap.containsKey(sessionId)) {
            sessionSolverMap[sessionId] = solver
            val solution = retrieveOrCreateSolution(sessionId)

            val bestSolution = solver.solve(solution)
            synchronized(this@VehicleRoutingSolverService) {
                solutionChange(sessionId, bestSolution)
                sessionSolverMap.remove(sessionId)
                statusChange(terminated, sessionId)
            }
        }
    }

    /**
     * Stops the specified solver (associated by the user sessionID) and change the solver status.
     *
     * @param sessionId user Http Session ID.
     */
    @Synchronized
    fun terminateEarly(sessionId: String): Boolean {
        val solver = sessionSolverMap.remove(sessionId)
        statusChange(terminated, sessionId)
        return if (solver != null) {
            solver.terminateEarly()
            true
        } else false
    }

    /**
     * Removes all information associated to the user (identified by sessionID).
     *
     * @param sessionId user Http Session ID.
     */
    @Synchronized
    fun clean(sessionId: String) {
        sessionSolverMap.remove(sessionId)?.terminateEarly()
        sessionSolutionMap.remove(sessionId)
        sessionDetailedView.remove(sessionId)
        sessionInstance.remove(sessionId)
        statusChange(terminated, sessionId)
    }

}
