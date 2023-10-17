package io.github.pintowar.opta.router.config.camel

import io.github.pintowar.opta.router.adapters.handler.WebSocketHandler
import io.github.pintowar.opta.router.core.solver.VrpSolverManager
import io.github.pintowar.opta.router.core.solver.VrpSolverService
import org.apache.camel.builder.RouteBuilder
import org.springframework.stereotype.Component

@Component
class CamelEventsRegistry : RouteBuilder() {

    override fun configure() {

        from("{{camel.route.consumer.request-solver}}")
            .routeId("request.solver.queue")
            .bean(
                VrpSolverManager::class.java,
                "solve(\${body.solverKey}, \${body.detailedSolution}, \${body.solverName})"
            )

        from("{{camel.route.consumer.solution-request}}")
            .routeId("solution.request.queue")
            .bean(VrpSolverService::class.java, "updateAndBroadcast(\${body.solutionRequest}, \${body.clear})")

        from("{{camel.route.consumer.solution-topic}}")
            .routeId("solution.topic")
            .transform().spel("#{body.messageObject}")
            .bean(WebSocketHandler::class.java, "broadcast(\${body.solutionRequest})")

        from("{{camel.route.consumer.cancel-solver-topic}}")
            .routeId("cancel.solver.topic")
            .transform().spel("#{body.messageObject}")
            .bean(
                VrpSolverManager::class.java,
                "cancelSolver(\${body.solverKey}, \${body.currentStatus}, \${body.clear})"
            )
    }

}