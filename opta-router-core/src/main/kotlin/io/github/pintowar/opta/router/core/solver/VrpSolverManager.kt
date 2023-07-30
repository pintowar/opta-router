package io.github.pintowar.opta.router.core.solver

import io.github.pintowar.opta.router.core.domain.models.SolverStatus
import io.github.pintowar.opta.router.core.domain.models.VrpDetailedSolution
import io.github.pintowar.opta.router.core.domain.models.VrpSolutionRequest
import io.github.pintowar.opta.router.core.domain.models.matrix.VrpCachedMatrix
import io.github.pintowar.opta.router.core.domain.ports.SolverEventsPort
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
    private val solverEvents: SolverEventsPort
) {

    private class UserCancellationException(val clear: Boolean) : CancellationException("User cancellation command.")

    private val supervisorJob = SupervisorJob()
    private val managerScope = CoroutineScope(supervisorJob + Dispatchers.Default)
    private val solverKeys = ConcurrentHashMap<UUID, Job>()
    private val blackListedKeys = ConcurrentHashMap.newKeySet<UUID>()

    init {
        solverEvents.addRequestSolverListener { solve(it.solverKey, it.detailedSolution, it.solverName) }
        solverEvents.addBroadcastCancelListener { cancelSolver(it.solverKey, it.currentStatus, it.clear) }
    }

    fun destroy() {
        supervisorJob.cancel()
    }

    private fun enqueueSolution(solutionRequest: VrpSolutionRequest, clear: Boolean = false) {
        solverEvents.enqueueSolutionRequest(SolverEventsPort.SolutionRequestCommand(solutionRequest, clear))
    }

    private fun solve(solverKey: UUID, detailedSolution: VrpDetailedSolution, solverName: String) {
        if (blackListedKeys.remove(solverKey)) {
            enqueueSolution(VrpSolutionRequest(detailedSolution.solution, SolverStatus.TERMINATED, solverKey))
            return
        }
        if (solverKeys.containsKey(solverKey)) return

        solverKeys[solverKey] = managerScope.launch {
            var bestSolution = detailedSolution.solution
            Solver
                .getSolverByName(solverName)
                .solve(detailedSolution.solution, VrpCachedMatrix(detailedSolution.matrix), SolverConfig(timeLimit))
                .onEach {
                    bestSolution = it
                    logger.info { "onEach: $solverKey | ${bestSolution.getTotalDistance()} ($solverName)" }
                    enqueueSolution(VrpSolutionRequest(it, SolverStatus.RUNNING, solverKey))
                }
                .onCompletion { ex ->
                    logger.info { "onEnd: $solverKey | ${bestSolution.getTotalDistance()} ($solverName)" }
                    val solRequest = VrpSolutionRequest(bestSolution, SolverStatus.TERMINATED, solverKey)
                    val shouldClear = ex is UserCancellationException && ex.clear
                    enqueueSolution(solRequest, shouldClear)
                }
                .collect()
        }
    }

    private fun cancelSolver(solverKey: UUID, currentStatus: SolverStatus, clear: Boolean) {
        if (currentStatus == SolverStatus.ENQUEUED) blackListedKeys.add(solverKey)
        solverKeys.remove(solverKey)?.cancel(UserCancellationException(clear))
    }
}