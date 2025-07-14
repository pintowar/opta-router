package io.github.pintowar.opta.router.config.camel.rest

import io.github.pintowar.opta.router.config.ConfigData
import io.github.pintowar.opta.router.core.domain.messages.SolutionCommand
import io.github.pintowar.opta.router.core.domain.messages.SolutionRequestCommand
import io.github.pintowar.opta.router.core.serialization.Serde
import io.github.pintowar.opta.router.core.serialization.fromCbor
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.component.reactive.streams.util.UnwrapStreamProcessor
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile(ConfigData.REST_PROFILE)
class CamelRestEvents(
    private val serde: Serde
) : RouteBuilder() {
    /**
     * Configures the Camel routes for the REST events.
     */
    override fun configure() {
        from("{{camel.route.consumer.enqueue-request-solver}}")
            .routeId("enqueue.request.solver")
            .transform()
            .body(serde::toCbor)
            .to("{{camel.route.producer.request-solver}}")

        from("{{camel.route.consumer.enqueue-solution-request}}")
            .routeId("enqueue.solution.request")
            .transform()
            .body(serde::toCbor)
            .to("{{camel.route.producer.solution-request}}")

        from("{{camel.route.consumer.solution-request}}")
            .routeId("solution.request.queue")
            .transform()
            .body { serde.fromCbor<SolutionRequestCommand>(it as ByteArray) }
            .bean(AsyncPipeRest::class.java, "update")
            .process(UnwrapStreamProcessor())
            .transform()
            .body(serde::toCbor)
            .to("{{camel.route.producer.solution-topic}}")

        from("{{camel.route.consumer.broadcast-solution}}")
            .routeId("broadcast.solution")
            .transform()
            .body(serde::toCbor)
            .to("{{camel.route.producer.solution-topic}}")

        from("{{camel.route.consumer.solution-topic}}")
            .routeId("solution.topic")
            .transform()
            .spel("#{body.messageObject}")
            .transform()
            .body { serde.fromCbor<SolutionCommand>(it as ByteArray) }
            .bean(AsyncPipeRest::class.java, "broadcast")
            .process(UnwrapStreamProcessor())
            .end()

        from("{{camel.route.consumer.broadcast-cancel-solver}}")
            .routeId("broadcast.broadcast.cancel.solver")
            .transform()
            .body(serde::toCbor)
            .to("{{camel.route.producer.cancel-solver-topic}}")
    }
}