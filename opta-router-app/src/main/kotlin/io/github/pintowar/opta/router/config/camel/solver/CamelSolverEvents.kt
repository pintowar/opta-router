package io.github.pintowar.opta.router.config.camel.solver

import io.github.pintowar.opta.router.config.ConfigData
import io.github.pintowar.opta.router.config.camel.SplitStreamProcessorTo
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.component.reactive.streams.util.UnwrapStreamProcessor
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile(ConfigData.SOLVER_PROFILE)
class CamelSolverEvents : RouteBuilder() {

    override fun configure() {
        from("{{camel.route.consumer.request-solver}}")
            .routeId("request.solver.queue")
            .bean(AsyncPipeSolver::class.java, "solve")
            .process(SplitStreamProcessorTo("{{camel.route.producer.solution-request}}", context))

        from("{{camel.route.consumer.cancel-solver-topic}}")
            .routeId("cancel.solver.topic")
            .transform().spel("#{body.messageObject}")
            .bean(AsyncPipeSolver::class.java, "cancelSolver")
            .process(UnwrapStreamProcessor())
            .end()
    }
}