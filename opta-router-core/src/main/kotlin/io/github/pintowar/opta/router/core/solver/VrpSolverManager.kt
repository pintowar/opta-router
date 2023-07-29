package io.github.pintowar.opta.router.core.solver

import io.github.pintowar.opta.router.core.domain.models.SolverStatus
import io.github.pintowar.opta.router.core.domain.models.VrpSolutionRequest
import io.github.pintowar.opta.router.core.domain.ports.BroadcastPort
import io.github.pintowar.opta.router.core.domain.ports.SolverEventsPort
import io.github.pintowar.opta.router.core.domain.repository.SolverRepository
import io.github.pintowar.opta.router.core.solver.spi.Solver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import java.time.Duration
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger {}

/**
 * This service is responsible to solve VRP problems asynchronously, update the application status, solver and solutions
 * storages. It also sends notifications to the WebSocket queue of the proper user (just for the one that executed the
 * application).
 *
 */
class VrpSolverManager(
    private val timeLimit: Duration,
    private val solverEvents: SolverEventsPort,
    private val solverRepository: SolverRepository,
) {

    private val supervisorJob = SupervisorJob()
    private val managerScope = CoroutineScope(supervisorJob + Dispatchers.Default)
    private val solverKeys = ConcurrentHashMap<UUID, Job>()

    init {
        solverEvents.addRequestSolverListener { solve(it.problemId, it.solverKey, it.solverName) }
        solverEvents.addBroadcastCancelListener { cancelSolver(it.solverKey, it.clear) }
    }

    fun destroy() {
        supervisorJob.cancel()
    }

    private fun enqueueSolution(solutionRequest: VrpSolutionRequest, clear: Boolean = false) {
        solverEvents.enqueueSolutionRequest(SolverEventsPort.SolutionRequestCommand(solutionRequest, clear))
    }

    private fun solve(problemId: Long, uuid: UUID, solverName: String) {
        if (solverKeys.containsKey(uuid)) return

        val currentMatrix = solverRepository.currentMatrix(problemId) ?: return
        val solutionRequest = solverRepository.currentSolutionRequest(problemId) ?: return
        val solverKey = solutionRequest.solverKey ?: return
        val currentSolution = solutionRequest.solution

        solverKeys[solverKey] = managerScope.launch {
            val scope = this
            var bestSolution = currentSolution
            Solver
                .getSolverByName(solverName)
                .solve(currentSolution, currentMatrix, SolverConfig(timeLimit))
                .onEach {
                    bestSolution = it
                    logger.debug { "onEach (${scope.isActive}): $solverKey | ${bestSolution.getTotalDistance()}" }
                    enqueueSolution(VrpSolutionRequest(it, SolverStatus.RUNNING, solverKey))
                }
                .onCompletion {
                    logger.debug { "onEnd (${scope.isActive}): $solverKey | ${bestSolution.getTotalDistance()}" }
                    enqueueSolution(VrpSolutionRequest(bestSolution, SolverStatus.TERMINATED, solverKey))
                }
                .collect()
        }
    }

    private fun cancelSolver(uuid: UUID, clear: Boolean) {
        solverRepository.currentSolverRequest(uuid)?.also { solverRequest ->
            if (solverRequest.status != SolverStatus.NOT_SOLVED) {
                runBlocking {
                    solverKeys.remove(uuid)?.cancelAndJoin()
                }
                if (clear) {
                    solverRepository.currentSolutionRequest(solverRequest.problemId)?.let {
                        enqueueSolution(it, true)
                    }
                }
            }
        }
    }
}