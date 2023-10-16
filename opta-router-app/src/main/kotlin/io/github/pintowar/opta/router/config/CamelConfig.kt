package io.github.pintowar.opta.router.config

import io.github.pintowar.opta.router.core.domain.ports.BroadcastPort
import io.github.pintowar.opta.router.core.domain.ports.SolverEventsPort
import org.apache.camel.ProducerTemplate
import org.springframework.stereotype.Component

@Component
class CamelConfig(
    private val template: ProducerTemplate,
) : SolverEventsPort, BroadcastPort {

    override fun enqueueRequestSolver(command: SolverEventsPort.RequestSolverCommand) {
        template.sendBody("direct:request-solver-direct", command)
    }

    override fun enqueueSolutionRequest(command: SolverEventsPort.SolutionRequestCommand) {
        template.sendBody("direct:solution-request-queue", command)
    }

    override fun broadcastSolution(command: BroadcastPort.SolutionCommand) {
        template.sendBody("direct:solution-topic", command)
    }

    override fun broadcastCancelSolver(command: SolverEventsPort.CancelSolverCommand) {
        template.sendBody("direct:cancel-solver-topic", command)
    }

}