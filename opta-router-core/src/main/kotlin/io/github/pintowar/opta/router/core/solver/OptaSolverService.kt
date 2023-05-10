package io.github.pintowar.opta.router.core.solver

import io.github.pintowar.opta.router.core.domain.models.SolverState
import io.github.pintowar.opta.router.core.domain.models.VrpProblem
import io.github.pintowar.opta.router.core.domain.models.VrpSolutionRegistry
import io.github.pintowar.opta.router.core.domain.ports.BroadcastPort
import io.github.pintowar.opta.router.core.domain.repository.SolverRepository
import mu.KotlinLogging
import org.optaplanner.core.api.solver.SolverFactory
import org.optaplanner.core.api.solver.SolverManager
import org.optaplanner.core.api.solver.SolverStatus
import org.optaplanner.core.config.solver.SolverConfig
import org.optaplanner.core.config.solver.termination.TerminationConfig
import org.optaplanner.examples.vehiclerouting.domain.VehicleRoutingSolution
import java.time.Duration
import java.util.*

private val logger = KotlinLogging.logger {}

/**
 * This service is responsible to solve VRP problems asynchronously, update the application status, solver and solutions
 * storages. It also sends notifications to the WebSocket queue of the proper user (just for the one that executed the
 * application).
 *
 */
class OptaSolverService(
    val timeLimit: Duration,
    val solverRepository: SolverRepository,
    val broadcastPort: BroadcastPort
) : VrpSolverService {

    val configPath = "org/optaplanner/examples/vehiclerouting/vehicleRoutingSolverConfig.xml"
    val solverConfig = SolverConfig.createFromXmlResource(configPath).apply {
        terminationConfig = TerminationConfig().apply {
            overwriteSpentLimit(timeLimit)
        }
    }
    val solverFactory = SolverFactory.create<VehicleRoutingSolution>(solverConfig)
    val solverManager = SolverManager.create<VehicleRoutingSolution, Long>(solverFactory)

    val solverName = "optaplanner"

    fun broadcastSolution(problemId: Long) {
        currentSolutionState(problemId)
            ?.let(broadcastPort::broadcastSolution)
    }

    override fun currentSolutionState(problemId: Long): VrpSolutionRegistry? =
        solverRepository.currentOrNewSolutionRegistry(problemId, solverName)

    override fun showState(problemId: Long): SolverState =
        currentSolutionState(problemId)?.state ?: SolverState.NOT_SOLVED

    override fun updateDetailedView(problemId: Long, enabled: Boolean) {
        broadcastSolution(problemId)
    }

    override fun asyncSolve(instance: VrpProblem) {
        if (solverManager.getSolverStatus(instance.id) == SolverStatus.NOT_SOLVING) {
            val currentSolutionRegistry = solverRepository.currentOrNewSolutionRegistry(instance.id, solverName)!!
            val currentMatrix = solverRepository.currentMatrix(instance.id) ?: return
            val solverKey = currentSolutionRegistry.solverKey ?: UUID.randomUUID()

            val solution = currentSolutionRegistry.solution.toSolverSolution(currentMatrix)
            solverRepository.insertNewSolution(
                solution.toDTO(instance, currentMatrix),
                solverName,
                solverKey,
                SolverState.ENQUEUED
            )
            try {
                solverManager.solveAndListen(
                    solution.id,
                    { it: Long -> if (it == solution.id) solution else null },
                    { sol: VehicleRoutingSolution ->
                        solverRepository.insertNewSolution(
                            sol.toDTO(instance, currentMatrix),
                            solverName,
                            solverKey,
                            SolverState.RUNNING
                        )
                        broadcastSolution(instance.id)
                    },
                    { sol: VehicleRoutingSolution ->
                        solverRepository.insertNewSolution(
                            sol.toDTO(instance, currentMatrix),
                            solverName,
                            solverKey,
                            SolverState.TERMINATED
                        )
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

    override fun terminateEarly(problemId: Long) {
        if (solverManager.getSolverStatus(problemId) != SolverStatus.NOT_SOLVING) {
            solverManager.terminateEarly(problemId)
            broadcastSolution(problemId)
        }
    }

    override fun clean(problemId: Long) {
        val current = solverRepository.currentOrNewSolutionRegistry(problemId, solverName)
        if (current != null) {
            solverManager.terminateEarly(problemId)
            solverRepository.clearSolution(problemId)
            broadcastSolution(problemId)
        }
    }
}