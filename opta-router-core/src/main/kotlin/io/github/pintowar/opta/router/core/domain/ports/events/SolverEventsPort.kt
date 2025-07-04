package io.github.pintowar.opta.router.core.domain.ports.events

import io.github.pintowar.opta.router.core.domain.messages.CancelSolverCommand
import io.github.pintowar.opta.router.core.domain.messages.RequestSolverCommand
import io.github.pintowar.opta.router.core.domain.messages.SolutionRequestCommand

/**
 * The SolverEventsPort is responsible for handling events related to the solver.
 */
interface SolverEventsPort {
    /**
     * Enqueues a request to the solver.
     *
     * @param command The [RequestSolverCommand] to enqueue.
     */
    fun enqueueRequestSolver(command: RequestSolverCommand)

    /**
     * Enqueues a solution request.
     *
     * @param command The [SolutionRequestCommand] to enqueue.
     */
    fun enqueueSolutionRequest(command: SolutionRequestCommand)

    /**
     * Broadcasts a command to cancel a solver.
     *
     * @param command The [CancelSolverCommand] to broadcast.
     */
    fun broadcastCancelSolver(command: CancelSolverCommand)
}