package io.github.pintowar.opta.router.core.solver

import io.github.pintowar.opta.router.core.domain.models.SolverState
import io.github.pintowar.opta.router.core.domain.models.VrpSolution
import io.github.pintowar.opta.router.core.domain.models.VrpSolutionRegistry
import io.github.pintowar.opta.router.core.domain.ports.BroadcastPort
import io.github.pintowar.opta.router.core.domain.repository.SolverRepository
import mu.KotlinLogging
import org.optaplanner.core.api.solver.Solver
import org.optaplanner.core.api.solver.SolverFactory
import org.optaplanner.core.config.solver.SolverConfig
import org.optaplanner.core.config.solver.termination.TerminationConfig
import org.optaplanner.examples.vehiclerouting.domain.VehicleRoutingSolution
import java.time.Duration
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService

private val logger = KotlinLogging.logger {}

/**
 * This service is responsible to solve VRP problems asynchronously, update the application status, solver and solutions
 * storages. It also sends notifications to the WebSocket queue of the proper user (just for the one that executed the
 * application).
 *
 */
class OptaSolverService(
    private val timeLimit: Duration,
    private val executor: ExecutorService,
    private val solverRepository: SolverRepository,
    private val broadcastPort: BroadcastPort
) : VrpSolverService {

    private val solverKeys = ConcurrentHashMap<UUID, Solver<VehicleRoutingSolution>>()

    val configPath = "org/optaplanner/examples/vehiclerouting/vehicleRoutingSolverConfig.xml"
    val solverConfig = SolverConfig.createFromXmlResource(configPath).apply {
        terminationConfig = TerminationConfig().apply {
            overwriteSpentLimit(timeLimit)
        }
    }
    val solverFactory = SolverFactory.create<VehicleRoutingSolution>(solverConfig)

    val solverName = "optaplanner"

    override fun currentSolutionRegistry(problemId: Long): VrpSolutionRegistry? =
        solverRepository.latestSolution(problemId)
            ?: solverRepository.latestOrNewSolutionRegistry(problemId, solverName, UUID.randomUUID())
    // Adjust this

    override fun showState(problemId: Long): SolverState =
        currentSolutionRegistry(problemId)?.state ?: SolverState.NOT_SOLVED

    override fun updateDetailedView(problemId: Long, enabled: Boolean) {
        broadcastSolution(problemId)
    }

    override fun enqueueSolverRequest(problemId: Long): UUID? {
        return solverRepository.enqueue(problemId, solverName)?.let { request ->
            executor.submit { solve(request.problemId, request.requestKey) }
            request.requestKey
        }
    }

    override fun terminateEarly(solverKey: UUID) {
        solverKeys[solverKey]?.terminateEarly()
//        if (solverManager.getSolverStatus(problemId) != SolverStatus.NOT_SOLVING) {
//            solverManager.terminateEarly(problemId)
//            broadcastSolution(problemId)
//        }
    }

    override fun clean(solverKey: UUID) {
        solverKeys[solverKey]?.terminateEarly()
        solverKeys.remove(solverKey)
//        val current = solverRepository.currentOrNewSolutionRegistry(problemId, solverName)
//        if (current != null) {
//            solverManager.terminateEarly(problemId)
//            solverRepository.clearSolution(problemId)
//            broadcastSolution(problemId)
//        }
    }

    fun solve(problemId: Long, uuid: UUID) {
        if (solverKeys.containsKey(uuid)) return

        val currentSolutionRegistry = solverRepository.latestOrNewSolutionRegistry(problemId, solverName, uuid)!!
        val currentMatrix = solverRepository.currentMatrix(problemId) ?: return
        val problem = currentSolutionRegistry.solution.problem
        val solverKey = currentSolutionRegistry.solverKey ?: UUID.randomUUID()

        solverKeys[solverKey] = solverFactory.buildSolver()

        val solution = currentSolutionRegistry.solution.toSolverSolution(currentMatrix)
        updateAndBroadcast(solution.toDTO(problem, currentMatrix), solverKey, SolverState.RUNNING)
        try {
            solverKeys[solverKey]!!.addEventListener { evt ->
                val sol = evt.newBestSolution
                updateAndBroadcast(sol.toDTO(problem, currentMatrix), solverKey, SolverState.RUNNING)
            }
            val sol = solverKeys[solverKey]!!.solve(solution)
            updateAndBroadcast(sol.toDTO(problem, currentMatrix), solverKey, SolverState.TERMINATED)
        } catch (e: IllegalStateException) {
            logger.warn(e) { "Instance ${problem.id} is already being solved." }
        }
    }

    fun broadcastSolution(problemId: Long) {
        currentSolutionRegistry(problemId)?.let(broadcastPort::broadcastSolution)
    }

    fun updateAndBroadcast(sol: VrpSolution, solverKey: UUID, status: SolverState) {
        solverRepository.insertNewSolution(sol, solverName, solverKey, status)
        broadcastSolution(sol.problem.id)
    }


}