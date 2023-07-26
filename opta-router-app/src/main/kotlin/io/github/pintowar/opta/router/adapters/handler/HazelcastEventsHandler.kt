package io.github.pintowar.opta.router.adapters.handler

import com.hazelcast.core.HazelcastInstance
import io.github.pintowar.opta.router.core.domain.ports.BroadcastPort
import io.github.pintowar.opta.router.core.domain.repository.SolverRepository
import io.github.pintowar.opta.router.core.solver.VrpSolverManager
import io.github.pintowar.opta.router.core.domain.ports.SolverQueuePort
import mu.KotlinLogging
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

private val logger = KotlinLogging.logger {}

class HazelcastEventsHandler(
    private val solver: VrpSolverManager,
    private val solverRepository: SolverRepository,
    private val broadcastPort: BroadcastPort,
    hz: HazelcastInstance
) {
    private val running = AtomicBoolean(true)
    private val requestSolverQueue = hz.getQueue<SolverQueuePort.RequestSolverCommand>("request-solver-queue")
    private val requestSolverEs = Executors.newSingleThreadExecutor()
    private val solutionRequestQueue = hz.getQueue<SolverQueuePort.SolutionRequestCommand>("solution-request-queue")
    private val solverRequestEs = Executors.newFixedThreadPool(2)

    init {
        requestSolverEs.submit { requestSolverListener() }
        repeat(2) { solverRequestEs.submit { solutionRequestListener() } }
    }

    private fun requestSolverListener() {
        logger.debug { "Starting request-solver-queue Handler" }
        while (running.get()) {
            val cmd = requestSolverQueue.take()
            logger.debug { "Taking RequestSolverCommand: $cmd" }
            solver.solve(cmd.problemId, cmd.uuid, cmd.solverName)
        }
    }

    private fun solutionRequestListener() {
        logger.debug { "Starting solution-request-queue Handler" }
        while (running.get()) {
            val cmd = solutionRequestQueue.take()
            logger.debug { "Taking SolutionRequestCommand: ${cmd.solutionRequest.solverKey}" }
            val (solRequest, clear) = cmd.solutionRequest to cmd.clear
            val newSolRequest = solverRepository
                .addNewSolution(solRequest.solution, solRequest.solverKey!!, solRequest.status, clear)
            broadcastPort.broadcastSolution(newSolRequest)
        }
    }

    fun destroy() {
        running.set(false)
        listOf(requestSolverEs, solverRequestEs).forEach { it.shutdown() }
    }
}