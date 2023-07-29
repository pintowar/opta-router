package io.github.pintowar.opta.router.core.solver

import io.github.pintowar.opta.router.core.domain.models.SolverStatus
import io.github.pintowar.opta.router.core.domain.models.VrpSolution
import io.github.pintowar.opta.router.core.domain.models.VrpSolutionRequest
import io.github.pintowar.opta.router.core.domain.models.matrix.Matrix
import io.github.pintowar.opta.router.core.domain.ports.SolverEventsPort
import io.github.pintowar.opta.router.core.domain.repository.SolverRepository
import io.github.pintowar.opta.router.core.solver.spi.Solver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import mu.KotlinLogging
import java.time.Duration
import java.util.UUID
import java.util.concurrent.CancellationException
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger {}

class VrpSolverManager(
    private val timeLimit: Duration,
    private val solverEvents: SolverEventsPort,
    private val solverRepository: SolverRepository,
) {

    private class UserCancellationException(val clear: Boolean) : CancellationException("User cancellation command.")

    private val supervisorJob = SupervisorJob()
    private val managerScope = CoroutineScope(supervisorJob + Dispatchers.Default)
    private val solverKeys = ConcurrentHashMap<UUID, Job>()

    init {
        solverEvents.addRequestSolverListener { fetchAndSolve(it.problemId, it.solverKey, it.solverName) }
        solverEvents.addBroadcastCancelListener { cancelSolver(it.solverKey, it.clear) }
    }

    fun destroy() {
        supervisorJob.cancel()
    }

    private fun enqueueSolution(solutionRequest: VrpSolutionRequest, clear: Boolean = false) {
        solverEvents.enqueueSolutionRequest(SolverEventsPort.SolutionRequestCommand(solutionRequest, clear))
    }

    private fun fetchAndSolve(problemId: Long, uuid: UUID, solverName: String) {
        if (solverKeys.containsKey(uuid)) return
        val currentMatrix = solverRepository.currentMatrix(problemId) ?: return
        val solutionRequest = solverRepository.currentSolutionRequest(problemId) ?: return
        val solverKey = solutionRequest.solverKey ?: return
        val currentSolution = solutionRequest.solution

        solve(solverKey, currentSolution, currentMatrix, solverName)
    }

    private fun solve(solverKey: UUID, currentSolution: VrpSolution, currentMatrix: Matrix, solverName: String) {
        if (solverKeys.containsKey(solverKey)) return

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
                .onCompletion { ex ->
                    logger.debug { "onEnd (${scope.isActive}): $solverKey | ${bestSolution.getTotalDistance()}" }
                    val solRequest = VrpSolutionRequest(bestSolution, SolverStatus.TERMINATED, solverKey)
                    val shouldClear = ex is UserCancellationException && ex.clear
                    enqueueSolution(solRequest, shouldClear)
                }
                .collect()
        }
    }

    private fun cancelSolver(uuid: UUID, clear: Boolean) {
        solverKeys.remove(uuid)?.cancel(UserCancellationException(clear))
    }
}