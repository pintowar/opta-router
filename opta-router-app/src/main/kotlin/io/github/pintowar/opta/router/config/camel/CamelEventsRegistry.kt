package io.github.pintowar.opta.router.config.camel

import com.hazelcast.core.HazelcastInstance
import io.github.pintowar.opta.router.adapters.handler.WebSocketHandler
import io.github.pintowar.opta.router.core.solver.VrpSolverManager
import io.github.pintowar.opta.router.core.solver.VrpSolverService
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.builder.endpoint.StaticEndpointBuilders.*
import org.apache.camel.component.hazelcast.HazelcastConstants
import org.apache.camel.component.hazelcast.HazelcastOperation
import org.apache.camel.component.hazelcast.queue.HazelcastQueueConsumerMode
import org.springframework.stereotype.Component

@Component
class CamelEventsRegistry(hz: HazelcastInstance) : RouteBuilder() {

    private val requestSolverQueue = hazelcastQueue("request-solver-queue").hazelcastInstanceName(hz.name)
    private val solutionRequestQueue = hazelcastQueue("solution-request-queue").hazelcastInstanceName(hz.name)

    private val solutionTopic = hazelcastTopic("solution-topic").hazelcastInstanceName(hz.name)
    private val cancelTopic = hazelcastTopic("cancel-solver-topic").hazelcastInstanceName(hz.name).reliable(true)

    override fun configure() {
        from(direct("request-solver-direct"))
            .setHeader(HazelcastConstants.OPERATION, constant(HazelcastOperation.PUT))
            .to(requestSolverQueue)

        from(requestSolverQueue.queueConsumerMode(HazelcastQueueConsumerMode.POLL))
            .bean(VrpSolverManager::class.java, "solve(\${body.solverKey}, \${body.detailedSolution}, \${body.solverName})")

        from(direct("solution-request-queue"))
            .setHeader(HazelcastConstants.OPERATION, constant(HazelcastOperation.PUT))
            .to(solutionRequestQueue)

        from(solutionRequestQueue.queueConsumerMode(HazelcastQueueConsumerMode.POLL))
            .bean(VrpSolverService::class.java, "updateAndBroadcast(\${body.solutionRequest}, \${body.clear})")

        from(direct("solution-topic"))
            .to(solutionTopic)

        from(solutionTopic)
            .transform().spel("#{body.messageObject}")
            .bean(WebSocketHandler::class.java, "broadcast(\${body.solutionRequest})")

        from(direct("cancel-solver-topic"))
            .to(cancelTopic)

        from(cancelTopic)
            .transform().spel("#{body.messageObject}")
            .bean(VrpSolverManager::class.java, "cancelSolver(\${body.solverKey}, \${body.currentStatus}, \${body.clear})")

    }

}