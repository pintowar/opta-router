package io.github.pintowar.opta.router.adapters.handler

import io.github.pintowar.opta.router.core.domain.ports.SolverQueuePort
import org.springframework.context.ApplicationEvent
import org.springframework.context.ApplicationEventPublisher

class RequestSolverEvent(val command: SolverQueuePort.RequestSolverCommand) : ApplicationEvent(command)

class SolutionRequestEvent(val command: SolverQueuePort.SolutionRequestCommand) : ApplicationEvent(command)

class SolverQueueAdapter(private val publisher: ApplicationEventPublisher) : SolverQueuePort {

    override fun requestSolver(command: SolverQueuePort.RequestSolverCommand) {
        publisher.publishEvent(RequestSolverEvent(command))
    }

    override fun updateAndBroadcast(command: SolverQueuePort.SolutionRequestCommand) {
        publisher.publishEvent(SolutionRequestEvent(command))
    }
}