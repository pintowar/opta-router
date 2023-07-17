package io.github.pintowar.opta.router.core.solver

import io.github.pintowar.opta.router.core.domain.models.SolverStatus
import io.github.pintowar.opta.router.core.domain.models.VrpSolutionRequest
import io.github.pintowar.opta.router.core.domain.ports.BroadcastPort
import io.github.pintowar.opta.router.core.domain.ports.SolverQueuePort
import io.github.pintowar.opta.router.core.domain.repository.SolverRepository
import io.github.pintowar.opta.router.core.solver.spi.Solver
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import mu.KotlinLogging
import java.time.Duration
import java.util.*
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
    private val solverQueue: SolverQueuePort,
    private val solverRepository: SolverRepository,
    private val broadcastPort: BroadcastPort
) {

    private val solverScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val solverKeys = ConcurrentHashMap<UUID, Job>()

    fun currentSolutionRequest(problemId: Long): VrpSolutionRequest? {
        return solverRepository.currentSolutionRequest(problemId)
    }

    fun showStatus(problemId: Long): SolverStatus =
        solverRepository.refreshAndGetCurrentSolverRequest(problemId, timeLimit)?.status ?: SolverStatus.NOT_SOLVED

    fun updateDetailedView(problemId: Long) {
        solverRepository.currentSolutionRequest(problemId)?.let(broadcastPort::broadcastSolution)
    }

    fun enqueueSolverRequest(problemId: Long, solverName: String): UUID? {
        return solverRepository.enqueue(problemId, solverName)?.let { request ->
            solverQueue.requestSolver(
                SolverQueuePort.RequestSolverCommand(
                    request.problemId,
                    request.requestKey,
                    solverName
                )
            )
            request.requestKey
        }
    }

    fun terminateEarly(solverKey: UUID) {
        val solverRequest = solverRepository.currentSolverRequest(solverKey)
        if (solverRequest?.status != SolverStatus.NOT_SOLVED) {
            solverKeys[solverKey]?.cancel()
        }
    }

    fun clean(solverKey: UUID) {
        val solverRequest = solverRepository.currentSolverRequest(solverKey)
        if (solverRequest != null) {
            runBlocking {
                solverKeys[solverKey]?.cancelAndJoin()
            }
            solverRepository.currentSolutionRequest(solverRequest.problemId)?.let {
                broadcastSolution(it, true)
            }
        }
    }

    fun destroy() {
        solverKeys.forEach { (k, _) ->
            terminateEarly(k)
        }
    }

    fun solverNames() = Solver.getNamedSolvers().keys

    fun solve(problemId: Long, uuid: UUID, solverName: String) {
        if (solverKeys.containsKey(uuid)) return

        val currentMatrix = solverRepository.currentMatrix(problemId) ?: return
        val solutionRequest = solverRepository.currentSolutionRequest(problemId) ?: return
        val solverKey = solutionRequest.solverKey ?: return
        val currentSolution = solutionRequest.solution

        solverKeys[solverKey] = solverScope.launch {
            Solver.getSolverByName(solverName).also { solver ->
                val initialRequest = SolutionFlow(currentSolution)
                var bestSolution = currentSolution

                solver.solutionFlow(currentSolution, currentMatrix, SolverConfig(timeLimit))
                    .filter { !it.body.isEmpty() }
                    .distinctUntilChanged { (best, bestStatus), (actual, actualStatus) ->
                        bestStatus == actualStatus && best.getTotalDistance() <= actual.getTotalDistance()
                    }
                    .onEach { bestSolution = it.body }
                    .onCompletion { ex ->
                        if (ex is CancellationException) {
                            broadcastSolution(VrpSolutionRequest(bestSolution, SolverStatus.TERMINATED, solverKey))
                        }
                    }
                    .fold(initialRequest) { acc, req ->
                        val hasWorstSol by lazy { req.body.getTotalDistance() > acc.body.getTotalDistance() }
                        val condition = req.isCompleted && hasWorstSol
                        (if (condition) req.copy(body = acc.body) else req).also {
                            broadcastSolution(
                                VrpSolutionRequest(
                                    it.body,
                                    if (it.isCompleted) SolverStatus.TERMINATED else SolverStatus.RUNNING,
                                    solverKey
                                )
                            )
                        }
                    }
            }
        }
    }

    private fun broadcastSolution(solutionRequest: VrpSolutionRequest, clear: Boolean = false) {
        logger.debug { "${solutionRequest.solverKey} - ${solutionRequest.status} - ${solutionRequest.solution.getTotalDistance()}" }
        solverQueue.updateAndBroadcast(SolverQueuePort.SolutionRequestCommand(solutionRequest, clear))
    }
}