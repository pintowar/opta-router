package io.github.pintowar.opta.router.config.camel

import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.asPublisher
import org.apache.camel.CamelContext
import org.apache.camel.Exchange
import org.apache.camel.Processor
import org.apache.camel.component.reactive.streams.api.CamelReactiveStreams
import org.reactivestreams.Publisher

internal class SplitStreamProcessorTo(
    context: CamelContext,
    private val uri: String,
    private val transform: (body: Any) -> Any = { it },
) : Processor {

    private val camelReactive = CamelReactiveStreams.get(context)
    override fun process(exchange: Exchange) {
        val body = exchange.`in`.body
        if (body is Publisher<*>) {
            val subscriber = camelReactive.subscriber(uri)
            body.asFlow().map { exchange.copy().apply { message.body = transform(it) } }.asPublisher().subscribe(subscriber)
        }
    }
}