package io.github.pintowar.opta.router.adapters.handler

import com.hazelcast.core.HazelcastInstance
import io.github.pintowar.opta.router.core.domain.ports.SolverEventsPort
import mu.KotlinLogging
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

private val logger = KotlinLogging.logger {}

class HazelcastEventsHandler(
    hz: HazelcastInstance
): SolverEventsPort {
    private val running = AtomicBoolean(true)
    private val requestSolverQueue = hz.getQueue<SolverEventsPort.RequestSolverCommand>("request-solver-queue")
    private val requestSolverListeners: MutableList<(SolverEventsPort.RequestSolverCommand) -> Unit> = mutableListOf()
    private val requestSolverEs = Executors.newSingleThreadExecutor()
    
    private val solutionRequestQueue = hz.getQueue<SolverEventsPort.SolutionRequestCommand>("solution-request-queue")
    private val solutionRequestListeners: MutableList<(SolverEventsPort.SolutionRequestCommand) -> Unit> = mutableListOf()
    private val solverRequestEs = Executors.newSingleThreadExecutor()

    init {
        requestSolverEs.submit { requestSolverListener() }
        solverRequestEs.submit { solutionRequestListener() }
    }

    override fun enqueueRequestSolver(command: SolverEventsPort.RequestSolverCommand) = requestSolverQueue.put(command)


    override fun addRequestSolverListener(listener: (SolverEventsPort.RequestSolverCommand) -> Unit) {
        requestSolverListeners.add(listener)
    }

    override fun enqueueSolutionRequest(command: SolverEventsPort.SolutionRequestCommand) =
        solutionRequestQueue.put(command)

    override fun addSolutionRequestListener(listener: (SolverEventsPort.SolutionRequestCommand) -> Unit) {
        solutionRequestListeners.add(listener)
    }

    private fun requestSolverListener() {
        logger.debug { "Starting request-solver-queue Handler" }
        while (running.get()) {
            val cmd = requestSolverQueue.take()
            logger.debug { "Taking RequestSolverCommand: $cmd" }
            requestSolverListeners.forEach { listener -> listener(cmd) }
        }
    }

    private fun solutionRequestListener() {
        logger.debug { "Starting solution-request-queue Handler" }
        while (running.get()) {
            val cmd = solutionRequestQueue.take()
            logger.debug { "Taking SolutionRequestCommand: ${cmd.solutionRequest.solverKey}" }
            solutionRequestListeners.forEach { listener -> listener(cmd) }
        }
    }

    fun destroy() {
        running.set(false)
        listOf(requestSolverEs, solverRequestEs).forEach { it.shutdown() }
    }
}