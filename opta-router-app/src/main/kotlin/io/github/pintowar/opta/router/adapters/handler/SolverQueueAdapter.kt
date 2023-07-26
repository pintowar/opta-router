package io.github.pintowar.opta.router.adapters.handler

import com.hazelcast.core.HazelcastInstance
import io.github.pintowar.opta.router.core.domain.ports.SolverQueuePort

class SolverQueueAdapter(hz: HazelcastInstance) : SolverQueuePort {

    private val requestSolverQueue = hz.getQueue<SolverQueuePort.RequestSolverCommand>("request-solver-queue")
    private val solutionRequestQueue = hz.getQueue<SolverQueuePort.SolutionRequestCommand>("solution-request-queue")

    override fun requestSolver(command: SolverQueuePort.RequestSolverCommand) {
        requestSolverQueue.put(command)
    }

    override fun updateAndBroadcast(command: SolverQueuePort.SolutionRequestCommand) {
        solutionRequestQueue.put(command)
    }
}
