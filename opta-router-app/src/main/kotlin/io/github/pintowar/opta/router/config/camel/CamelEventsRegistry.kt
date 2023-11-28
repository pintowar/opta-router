package io.github.pintowar.opta.router.config.camel

import io.github.pintowar.opta.router.config.AsyncPipe
import io.github.pintowar.opta.router.core.solver.VrpSolverManager
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.component.reactive.streams.util.UnwrapStreamProcessor
import org.apache.camel.throttling.ThrottlingInflightRoutePolicy
import org.springframework.stereotype.Component

@Component
class CamelEventsRegistry : RouteBuilder() {

    override fun configure() {

        val incomingThrottling = ThrottlingInflightRoutePolicy().apply {
            maxInflightExchanges = 4
            resumePercentOfMax = 50
        }

        from("{{camel.route.consumer.request-solver}}")
            .routeId("request.solver.queue")
            .bean(
                VrpSolverManager::class.java,
                "solve(\${body.solverKey}, \${body.detailedSolution}, \${body.solverName})"
            )

        from("{{camel.route.consumer.solution-request}}")
            .routeId("solution.request.queue")
            .routePolicy(incomingThrottling)
            .bean(AsyncPipe::class.java, "updateAndBroadcast(\${body.solutionRequest}, \${body.clear})")
            .process(UnwrapStreamProcessor())

        from("{{camel.route.consumer.solution-topic}}")
            .routeId("solution.topic")
            .routePolicy(incomingThrottling)
            .transform().spel("#{body.messageObject}")
            .bean(AsyncPipe::class.java, "broadcast(\${body.solutionRequest})")
            .process(UnwrapStreamProcessor())

        from("{{camel.route.consumer.cancel-solver-topic}}")
            .routeId("cancel.solver.topic")
            .transform().spel("#{body.messageObject}")
            .bean(
                VrpSolverManager::class.java,
                "cancelSolver(\${body.solverKey}, \${body.currentStatus}, \${body.clear})"
            )
    }

}