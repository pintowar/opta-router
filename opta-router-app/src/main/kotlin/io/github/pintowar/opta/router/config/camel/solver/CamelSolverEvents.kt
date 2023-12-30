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
//        from("{{camel.route.consumer.enqueue-request-solver}}")
//            .routeId("enqueue.request.solver")
//            .setHeader(HazelcastConstants.OPERATION, constant(HazelcastOperation.PUT))
//            .to("{{camel.route.producer.request-solver}}")

        from("{{camel.route.consumer.request-solver}}")
            .routeId("request.solver.queue")
            .bean(AsyncPipeSolver::class.java, "solve")
            .process(SplitStreamProcessorTo("{{camel.route.producer.solution-request}}", context))

//        from("{{camel.route.consumer.enqueue-solution-request}}")
//            .routeId("enqueue.solution.request")
//            .setHeader(HazelcastConstants.OPERATION, constant(HazelcastOperation.PUT))
//            .to("{{camel.route.producer.solution-request}}")

//        from("{{camel.route.consumer.solution-request}}")
//            .routeId("solution.request.queue")
//            .bean(AsyncPipe::class.java, "update")
//            .process(UnwrapStreamProcessor())
//            .to("{{camel.route.producer.solution-topic}}")

//        from("{{camel.route.consumer.broadcast-solution}}")
//            .routeId("broadcast.solution")
//            .to("{{camel.route.producer.solution-topic}}")

//        from("{{camel.route.consumer.solution-topic}}")
//            .routeId("solution.topic")
//            .transform().spel("#{body.messageObject}")
//            .bean(AsyncPipe::class.java, "broadcast")
//            .process(UnwrapStreamProcessor())
//            .end()

//        from("{{camel.route.consumer.broadcast-cancel-solver}}")
//            .routeId("broadcast.broadcast.cancel.solver")
//            .to("{{camel.route.producer.cancel-solver-topic}}")

        from("{{camel.route.consumer.cancel-solver-topic}}")
            .routeId("cancel.solver.topic")
            .transform().spel("#{body.messageObject}")
            .bean(AsyncPipeSolver::class.java, "cancelSolver")
            .process(UnwrapStreamProcessor())
            .end()
    }
}