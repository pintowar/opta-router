package io.github.pintowar.opta.router.config.camel.solver

import io.github.pintowar.opta.router.config.ConfigData
import io.github.pintowar.opta.router.config.camel.SplitStreamProcessorTo
import io.github.pintowar.opta.router.core.domain.messages.CancelSolverCommand
import io.github.pintowar.opta.router.core.domain.messages.RequestSolverCommand
import io.github.pintowar.opta.router.core.serialization.Serde
import io.github.pintowar.opta.router.core.serialization.fromCbor
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.component.reactive.streams.util.UnwrapStreamProcessor
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile(ConfigData.SOLVER_PROFILE)
class CamelSolverEvents(
    private val serde: Serde
) : RouteBuilder() {
    /**
     * Configures the Camel routes for the solver events.
     */
    override fun configure() {
        from("{{camel.route.consumer.request-solver}}")
            .routeId("request.solver.queue")
            .transform()
            .body { serde.fromCbor<RequestSolverCommand>(it as ByteArray) }
            .bean(AsyncPipeSolver::class.java, "solve")
            .process(SplitStreamProcessorTo(context, "{{camel.route.producer.solution-request}}", serde::toCbor))

        from("{{camel.route.consumer.cancel-solver-topic}}")
            .routeId("cancel.solver.topic")
            .transform()
            .spel("#{body.messageObject}")
            .transform()
            .body { serde.fromCbor<CancelSolverCommand>(it as ByteArray) }
            .bean(AsyncPipeSolver::class.java, "cancelSolver")
            .process(UnwrapStreamProcessor())
            .end()
    }
}