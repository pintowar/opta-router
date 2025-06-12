package io.github.pintowar.opta.router.core.solver

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.pintowar.opta.router.core.domain.messages.SolutionRequestCommand
import io.github.pintowar.opta.router.core.domain.models.SolverStatus
import io.github.pintowar.opta.router.core.domain.models.VrpDetailedSolution
import io.github.pintowar.opta.router.core.domain.models.VrpSolutionRequest
import io.github.pintowar.opta.router.core.domain.models.matrix.VrpCachedMatrix
import io.github.pintowar.opta.router.core.solver.spi.Solver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import java.time.Duration
import java.util.UUID
import java.util.concurrent.CancellationException
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger {}

class VrpSolverManager(timeLimit: Duration) {
    private class UserCancellationException(val clear: Boolean) : CancellationException("User cancellation command.")

    private val supervisorJob = SupervisorJob()
    private val managerScope = CoroutineScope(supervisorJob + Dispatchers.Default)
    private val solverKeys = ConcurrentHashMap<UUID, Job>()
    private val blackListedKeys = ConcurrentHashMap.newKeySet<UUID>()
    private val solverConfig = SolverConfig(timeLimit)

    fun solve(
        solverKey: UUID,
        detailedSolution: VrpDetailedSolution,
        solverName: String
    ): Flow<SolutionRequestCommand> {
        if (blackListedKeys.remove(solverKey)) {
            val cmd = wrapCommand(VrpSolutionRequest(detailedSolution.solution, SolverStatus.TERMINATED, solverKey))
            return flowOf(cmd)
        }
        if (solverKeys.containsKey(solverKey)) return emptyFlow()

        val channel = Channel<SolutionRequestCommand>()
        var bestSolution = detailedSolution.solution

        solverKeys[solverKey] =
            Solver
                .getSolverByName(solverName)
                .solve(detailedSolution.solution, VrpCachedMatrix(detailedSolution.matrix), solverConfig)
                .onEach {
                    bestSolution = it
                    logger.info { "onEach: $solverKey | ${bestSolution.getTotalDistance()} ($solverName)" }
                    channel.send(wrapCommand(VrpSolutionRequest(it, SolverStatus.RUNNING, solverKey)))
                }
                .onCompletion { ex ->
                    logger.info { "onEnd: $solverKey | ${bestSolution.getTotalDistance()} ($solverName)" }
                    val solRequest = VrpSolutionRequest(bestSolution, SolverStatus.TERMINATED, solverKey)
                    val shouldClear = ex is UserCancellationException && ex.clear
                    channel.send(wrapCommand(solRequest, shouldClear))
                    channel.close()
                }
                .launchIn(managerScope)

        return channel.receiveAsFlow()
    }

    suspend fun cancelSolver(
        solverKey: UUID,
        currentStatus: SolverStatus,
        clear: Boolean
    ) {
        if (currentStatus == SolverStatus.ENQUEUED) blackListedKeys.add(solverKey)
        solverKeys.remove(solverKey)?.let {
            it.cancel(UserCancellationException(clear))
            it.join()
        }
    }

    fun destroy() {
        supervisorJob.cancel()
    }

    private fun wrapCommand(
        solutionRequest: VrpSolutionRequest,
        clear: Boolean = false
    ) = SolutionRequestCommand(solutionRequest, clear)
}