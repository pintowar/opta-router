package io.github.pintowar.opta.router.core.solver

import io.github.pintowar.opta.router.core.domain.models.SolverState
import io.github.pintowar.opta.router.core.domain.models.VrpSolutionRegistry
import io.github.pintowar.opta.router.core.domain.ports.BroadcastPort
import io.github.pintowar.opta.router.core.domain.repository.SolverRepository
import io.github.pintowar.opta.router.core.solver.spi.Solver
import io.github.pintowar.opta.router.core.solver.spi.SolverFactory
import mu.KotlinLogging
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
class VrpSolverService(
    private val timeLimit: Duration,
    private val executor: ExecutorService,
//    private val solverQueue: SolverQueuePort,
    private val solverRepository: SolverRepository,
    private val broadcastPort: BroadcastPort
) {

    private val solverKeys = ConcurrentHashMap<UUID, Solver>()

    fun currentSolutionRegistry(problemId: Long): VrpSolutionRegistry? =
        solverRepository.latestSolution(problemId)
            ?: solverRepository.latestOrNewSolutionRegistry(problemId, UUID.randomUUID())
    //TODO: Adjust this

    fun showState(problemId: Long): SolverState =
        currentSolutionRegistry(problemId)?.state ?: SolverState.NOT_SOLVED

    fun updateDetailedView(problemId: Long) {
        currentSolutionRegistry(problemId)?.let(broadcastPort::broadcastSolution)
    }

    fun enqueueSolverRequest(problemId: Long): UUID? {
        val solverName = "optaplanner"
        return solverRepository.enqueue(problemId, solverName)?.let { request ->
//            solverQueue.requestSolver(request.problemId, request.requestKey)
            executor.submit { solve(request.problemId, request.requestKey, solverName) }
            request.requestKey
        }
    }

    fun terminateEarly(solverKey: UUID) {
        solverKeys[solverKey]?.terminate()
//        if (solverManager.getSolverStatus(problemId) != SolverStatus.NOT_SOLVING) {
//            solverManager.terminateEarly(problemId)
//            broadcastSolution(problemId)
//        }
    }

    fun clean(solverKey: UUID) {
        solverKeys[solverKey]?.terminate()
        solverKeys.remove(solverKey)
//        val current = solverRepository.currentOrNewSolutionRegistry(problemId, solverName)
//        if (current != null) {
//            solverManager.terminateEarly(problemId)
//            solverRepository.clearSolution(problemId)
//            broadcastSolution(problemId)
//        }
    }

    fun solve(problemId: Long, uuid: UUID, solverName: String) {
        if (solverKeys.containsKey(uuid)) return

        val currentSolutionRegistry = solverRepository.latestOrNewSolutionRegistry(problemId, uuid)!!
        val currentMatrix = solverRepository.currentMatrix(problemId) ?: return
        val solverKey = currentSolutionRegistry.solverKey ?: UUID.randomUUID()

        solverKeys[solverKey] = SolverFactory.createSolver(solverName, solverKey, SolverConfig(timeLimit))
        solverKeys[solverKey]!!.solve(currentSolutionRegistry.solution, currentMatrix) {
            solverRepository.insertNewSolution(it.solution, it.solverKey!!, it.state)
            broadcastPort.broadcastSolution(it)
        }
    }

}