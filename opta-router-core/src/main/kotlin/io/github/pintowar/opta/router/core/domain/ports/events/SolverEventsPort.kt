package io.github.pintowar.opta.router.core.domain.ports.events

import io.github.pintowar.opta.router.core.domain.messages.CancelSolverCommand
import io.github.pintowar.opta.router.core.domain.messages.RequestSolverCommand
import io.github.pintowar.opta.router.core.domain.messages.SolutionRequestCommand

interface SolverEventsPort {

    fun enqueueRequestSolver(command: RequestSolverCommand)

    fun enqueueSolutionRequest(command: SolutionRequestCommand)

    fun broadcastCancelSolver(command: CancelSolverCommand)
}