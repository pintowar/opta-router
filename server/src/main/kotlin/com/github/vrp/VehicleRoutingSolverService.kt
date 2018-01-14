package com.github.opta

import com.github.util.GraphWrapper
import com.github.vrp.Instance
import com.github.vrp.convertSolution
import com.github.vrp.dist.PathDistance
import org.optaplanner.core.api.solver.Solver
import org.optaplanner.core.api.solver.SolverFactory
import org.optaplanner.core.config.solver.termination.TerminationConfig
import org.optaplanner.examples.vehiclerouting.domain.VehicleRoutingSolution
import org.slf4j.LoggerFactory
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.annotation.PreDestroy

@Service
class VehicleRoutingSolverService(val graph: GraphWrapper, val messagingTemplate: SimpMessageSendingOperations) {

    private val solverFactory: SolverFactory<VehicleRoutingSolution> = SolverFactory.createFromXmlResource(SOLVER_CONFIG)

    private val sessionSolutionMap = HashMap<String, VehicleRoutingSolution>()
    private val sessionSolverMap = HashMap<String, Solver<VehicleRoutingSolution>>()
    private val sessionStatusMap = ConcurrentHashMap<String, String>()
    private val sessionDetailedView = ConcurrentHashMap<String, Boolean>()

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

    @PreDestroy
    @Synchronized
    fun destroy() {
        for (solver in sessionSolverMap.values) {
            solver.terminateEarly()
        }
    }

    fun statusChange(status: String, sessionId: String) {
        sessionStatusMap[sessionId] = status
        messagingTemplate.convertAndSend("/topic/status", status)
    }

    fun showStatus(sessionId: String) : String = sessionStatusMap.getOrDefault(sessionId, "")

    fun changeDetailedView(sessionId: String, enabled: Boolean) {
        sessionDetailedView[sessionId] = enabled
    }

    fun isViewDetailed(sessionId: String) = sessionDetailedView.getOrDefault(sessionId, false)

    @Synchronized
    fun retrieveOrCreateSolution(sessionId: String, instance: Instance? = null): VehicleRoutingSolution? {
        var solution: VehicleRoutingSolution? = sessionSolutionMap[sessionId]
        if (solution == null && instance != null) {
            statusChange(calculatingDistances, sessionId)
            val points = instance.stops.map { it.toPair() }
            val method = PathDistance(points, graph)
            solution = instance.toSolution(method)
            sessionSolutionMap.put(sessionId, solution)
            statusChange(distancesCalculated, sessionId)
        }
        return solution
    }

    @Async
    fun solve(sessionId: String, json: Instance) {
        retrieveOrCreateSolution(sessionId, json)
        val solver = solverFactory.buildSolver()
        solver.addEventListener { event ->
            val bestSolution = event.newBestSolution
            val sol = bestSolution.convertSolution(if (isViewDetailed(sessionId)) graph else null)
            LOGGER.info("Best distance so far: " + sol.getTotalDistance())
            synchronized(this@VehicleRoutingSolverService) {
                sessionSolutionMap.put(sessionId, bestSolution)
                messagingTemplate.convertAndSend("/topic/solution", sol)
                statusChange(running, sessionId)
            }
        }
        if (!sessionSolverMap.containsKey(sessionId)) {
            sessionSolverMap.put(sessionId, solver)
            val solution = retrieveOrCreateSolution(sessionId)

            val bestSolution = solver.solve(solution)
            synchronized(this@VehicleRoutingSolverService) {
                sessionSolutionMap.put(sessionId, bestSolution)
                sessionSolverMap.remove(sessionId)
                statusChange(terminated, sessionId)
            }
        }
    }

    @Synchronized
    fun terminateEarly(sessionId: String): Boolean {
        val solver = sessionSolverMap.remove(sessionId)
        sessionStatusMap[sessionId] = terminated
        if (solver != null) {
            solver.terminateEarly()
            return true
        } else {
            return false
        }
    }

    companion object {

        private val SOLVER_CONFIG = "org/optaplanner/examples/vehiclerouting/solver/vehicleRoutingSolverConfig.xml"

        internal val LOGGER = LoggerFactory.getLogger(VehicleRoutingSolverService::class.java)
    }

}
