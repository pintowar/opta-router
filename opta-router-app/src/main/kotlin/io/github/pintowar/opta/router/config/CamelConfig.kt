package io.github.pintowar.opta.router.config

import io.github.pintowar.opta.router.core.domain.ports.BroadcastPort
import io.github.pintowar.opta.router.core.domain.ports.SolverEventsPort
import org.apache.camel.ProducerTemplate
import org.apache.camel.component.hazelcast.HazelcastConstants
import org.apache.camel.component.hazelcast.HazelcastOperation
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class CamelConfig(
    @Value("\${camel.route.producer.request-solver}") private val requestSolver: String,
    @Value("\${camel.route.producer.solution-request}") private val solutionRequest: String,
    @Value("\${camel.route.producer.solution-topic}") private val solutionTopic: String,
    @Value("\${camel.route.producer.cancel-solver-topic}") private val cancelSolverTopic: String,
    private val template: ProducerTemplate,
) : SolverEventsPort, BroadcastPort {

    private val hazelHeaders = mapOf(HazelcastConstants.OPERATION to HazelcastOperation.PUT)

    override fun enqueueRequestSolver(command: SolverEventsPort.RequestSolverCommand) {
        template.sendBodyAndHeaders(requestSolver, command, hazelHeaders)
    }

    override fun enqueueSolutionRequest(command: SolverEventsPort.SolutionRequestCommand) {
        template.sendBodyAndHeaders(solutionRequest, command, hazelHeaders)
    }

    override fun broadcastSolution(command: BroadcastPort.SolutionCommand) {
        template.sendBody(solutionTopic, command)
    }

    override fun broadcastCancelSolver(command: SolverEventsPort.CancelSolverCommand) {
        template.sendBody(cancelSolverTopic, command)
    }

}