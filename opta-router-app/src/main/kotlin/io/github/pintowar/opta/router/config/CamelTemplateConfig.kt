package io.github.pintowar.opta.router.config

import io.github.pintowar.opta.router.core.domain.messages.CancelSolverCommand
import io.github.pintowar.opta.router.core.domain.messages.RequestSolverCommand
import io.github.pintowar.opta.router.core.domain.messages.SolutionCommand
import io.github.pintowar.opta.router.core.domain.messages.SolutionRequestCommand
import io.github.pintowar.opta.router.core.domain.ports.events.BroadcastPort
import io.github.pintowar.opta.router.core.domain.ports.events.SolverEventsPort
import org.apache.camel.ProducerTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * This class is responsible for configuring the Camel templates for the solver and broadcast events.
 *
 * @param enqueueRequestSolver The Camel route for enqueuing a solver request.
 * @param enqueueSolutionRequest The Camel route for enqueuing a solution request.
 * @param broadcastSolution The Camel route for broadcasting a solution.
 * @param broadcastCancelSolver The Camel route for broadcasting a cancel solver command.
 * @param template The Camel producer template.
 */
@Component
class CamelTemplateConfig(
    @param:Value($$"${camel.route.producer.enqueue-request-solver}") private val enqueueRequestSolver: String,
    @param:Value($$"${camel.route.producer.enqueue-solution-request}") private val enqueueSolutionRequest: String,
    @param:Value($$"${camel.route.producer.broadcast-solution}") private val broadcastSolution: String,
    @param:Value($$"${camel.route.producer.broadcast-cancel-solver}") private val broadcastCancelSolver: String,
    private val template: ProducerTemplate
) : SolverEventsPort,
    BroadcastPort {
    /**
     * Enqueues a solver request command.
     *
     * @param command The command to enqueue.
     */
    override fun enqueueRequestSolver(command: RequestSolverCommand) {
        template.sendBody(enqueueRequestSolver, command)
    }

    /**
     * Enqueues a solution request command.
     *
     * @param command The command to enqueue.
     */
    override fun enqueueSolutionRequest(command: SolutionRequestCommand) {
        template.sendBody(enqueueSolutionRequest, command)
    }

    /**
     * Broadcasts a solution command.
     *
     * @param command The command to broadcast.
     */
    override fun broadcastSolution(command: SolutionCommand) {
        template.sendBody(broadcastSolution, command)
    }

    /**
     * Broadcasts a cancel solver command.
     *
     * @param command The command to broadcast.
     */
    override fun broadcastCancelSolver(command: CancelSolverCommand) {
        template.sendBody(broadcastCancelSolver, command)
    }
}