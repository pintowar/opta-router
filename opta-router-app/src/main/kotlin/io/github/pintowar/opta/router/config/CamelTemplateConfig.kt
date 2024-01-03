package io.github.pintowar.opta.router.config

import io.github.pintowar.opta.router.core.domain.messages.CancelSolverCommand
import io.github.pintowar.opta.router.core.domain.messages.RequestSolverCommand
import io.github.pintowar.opta.router.core.domain.messages.SolutionCommand
import io.github.pintowar.opta.router.core.domain.messages.SolutionRequestCommand
import io.github.pintowar.opta.router.core.domain.ports.events.BroadcastPort
import io.github.pintowar.opta.router.core.domain.ports.events.SolverEventsPort
import org.apache.camel.ProducerTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class CamelTemplateConfig(
    @Value("\${camel.route.producer.enqueue-request-solver}") private val enqueueRequestSolver: String,
    @Value("\${camel.route.producer.enqueue-solution-request}") private val enqueueSolutionRequest: String,
    @Value("\${camel.route.producer.broadcast-solution}") private val broadcastSolution: String,
    @Value("\${camel.route.producer.broadcast-cancel-solver}") private val broadcastCancelSolver: String,
    private val template: ProducerTemplate
) : SolverEventsPort, BroadcastPort {

    override fun enqueueRequestSolver(command: RequestSolverCommand) {
        template.sendBody(enqueueRequestSolver, command)
    }

    override fun enqueueSolutionRequest(command: SolutionRequestCommand) {
        template.sendBody(enqueueSolutionRequest, command)
    }

    override fun broadcastSolution(command: SolutionCommand) {
        template.sendBody(broadcastSolution, command)
    }

    override fun broadcastCancelSolver(command: CancelSolverCommand) {
        template.sendBody(broadcastCancelSolver, command)
    }
}