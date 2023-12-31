package io.github.pintowar.opta.router.config.camel.rest

import io.github.pintowar.opta.router.config.ConfigData
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.component.hazelcast.HazelcastConstants
import org.apache.camel.component.hazelcast.HazelcastOperation
import org.apache.camel.component.reactive.streams.util.UnwrapStreamProcessor
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile(ConfigData.REST_PROFILE)
class CamelRestEvents : RouteBuilder() {

    override fun configure() {
        from("{{camel.route.consumer.enqueue-request-solver}}")
            .routeId("enqueue.request.solver")
            .setHeader(HazelcastConstants.OPERATION, constant(HazelcastOperation.PUT))
            .to("{{camel.route.producer.request-solver}}")

        from("{{camel.route.consumer.enqueue-solution-request}}")
            .routeId("enqueue.solution.request")
            .setHeader(HazelcastConstants.OPERATION, constant(HazelcastOperation.PUT))
            .to("{{camel.route.producer.solution-request}}")

        from("{{camel.route.consumer.solution-request}}")
            .routeId("solution.request.queue")
            .bean(AsyncPipeRest::class.java, "update")
            .process(UnwrapStreamProcessor())
            .to("{{camel.route.producer.solution-topic}}")

        from("{{camel.route.consumer.broadcast-solution}}")
            .routeId("broadcast.solution")
            .to("{{camel.route.producer.solution-topic}}")

        from("{{camel.route.consumer.solution-topic}}")
            .routeId("solution.topic")
            .transform().spel("#{body.messageObject}")
            .bean(AsyncPipeRest::class.java, "broadcast")
            .process(UnwrapStreamProcessor())
            .end()

        from("{{camel.route.consumer.broadcast-cancel-solver}}")
            .routeId("broadcast.broadcast.cancel.solver")
            .to("{{camel.route.producer.cancel-solver-topic}}")
    }
}