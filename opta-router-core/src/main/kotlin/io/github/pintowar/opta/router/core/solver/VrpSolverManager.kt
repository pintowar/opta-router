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
    private val broadcastPort: BroadcastPort
) {

    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val solverKeys = ConcurrentHashMap<UUID, Job>()

    init {
        solverEvents.addRequestSolverListener { solve(it.problemId, it.uuid, it.solverName) }
        solverEvents.addSolutionRequestListener { updateAndBroadcast(it.solutionRequest, it.clear) }
    }

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
            solverEvents.enqueueRequestSolver(
                SolverEventsPort.RequestSolverCommand(
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
                enqueueSolution(it, true)
            }
        }
    }

    fun destroy() {
        solverKeys.forEach { (k, _) ->
            terminateEarly(k)
        }
    }

    fun solverNames() = Solver.getNamedSolvers().keys

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
            Solver.getSolverByName(solverName).also { solver ->
                var bestSolution = currentSolution

                solver.solve(currentSolution, currentMatrix, SolverConfig(timeLimit))
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
    }

    private fun updateAndBroadcast(solRequest: VrpSolutionRequest, clear: Boolean) {
        val newSolRequest = solverRepository
            .addNewSolution(solRequest.solution, solRequest.solverKey!!, solRequest.status, clear)
        broadcastPort.broadcastSolution(newSolRequest)
    }
}