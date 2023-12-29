package io.github.pintowar.opta.router.config.camel

import io.github.pintowar.opta.router.config.AsyncPipe
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.asPublisher
import org.apache.camel.CamelContext
import org.apache.camel.Exchange
import org.apache.camel.Processor
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.component.hazelcast.HazelcastConstants
import org.apache.camel.component.hazelcast.HazelcastOperation
import org.apache.camel.component.reactive.streams.api.CamelReactiveStreams
import org.apache.camel.component.reactive.streams.util.UnwrapStreamProcessor
import org.reactivestreams.Publisher
import org.springframework.stereotype.Component

@Component
class CamelEventsRegistry : RouteBuilder() {

    override fun configure() {
        from("{{camel.route.consumer.enqueue-request-solver}}")
            .routeId("enqueue.request.solver")
            .setHeader(HazelcastConstants.OPERATION, constant(HazelcastOperation.PUT))
            .to("{{camel.route.producer.request-solver}}")

        from("{{camel.route.consumer.enqueue-solution-request}}")
            .routeId("enqueue.solution.request")
            .setHeader(HazelcastConstants.OPERATION, constant(HazelcastOperation.PUT))
            .to("{{camel.route.producer.solution-request}}")

        from("{{camel.route.consumer.broadcast-solution}}")
            .routeId("broadcast.solution")
            .to("{{camel.route.producer.solution-topic}}")

        from("{{camel.route.consumer.broadcast-cancel-solver}}")
            .routeId("broadcast.broadcast.cancel.solver")
            .to("{{camel.route.producer.cancel-solver-topic}}")

        from("{{camel.route.consumer.request-solver}}")
            .routeId("request.solver.queue")
            .bean(AsyncPipe::class.java, "solve")
            .process(SplitStreamProcessorTo("{{camel.route.producer.solution-request}}", context))

        from("{{camel.route.consumer.solution-request}}")
            .routeId("solution.request.queue")
            .bean(AsyncPipe::class.java, "update")
            .process(UnwrapStreamProcessor())
            .to("{{camel.route.producer.solution-topic}}")

        from("{{camel.route.consumer.solution-topic}}")
            .routeId("solution.topic")
            .transform().spel("#{body.messageObject}")
            .bean(AsyncPipe::class.java, "broadcast")
            .process(UnwrapStreamProcessor())
            .end()

        from("{{camel.route.consumer.cancel-solver-topic}}")
            .routeId("cancel.solver.topic")
            .transform().spel("#{body.messageObject}")
            .bean(AsyncPipe::class.java, "cancelSolver")
            .process(UnwrapStreamProcessor())
            .end()
    }

    private class SplitStreamProcessorTo(private val uri: String, context: CamelContext) : Processor {

        private val camelReactive = CamelReactiveStreams.get(context)
        override fun process(exchange: Exchange) {
            val body = exchange.`in`.body
            if (body is Publisher<*>) {
                val subscriber = camelReactive.subscriber(uri)
                body.asFlow().map { exchange.copy().apply { message.body = it } }.asPublisher().subscribe(subscriber)
            }
        }
    }
}