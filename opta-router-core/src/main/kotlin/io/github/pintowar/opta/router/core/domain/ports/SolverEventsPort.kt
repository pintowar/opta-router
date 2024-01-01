package io.github.pintowar.opta.router.core.domain.ports

import io.github.pintowar.opta.router.core.domain.messages.CancelSolverCommand
import io.github.pintowar.opta.router.core.domain.messages.RequestSolverCommand
import io.github.pintowar.opta.router.core.domain.messages.SolutionRequestCommand
import io.github.pintowar.opta.router.core.domain.models.SolverStatus
import io.github.pintowar.opta.router.core.domain.models.VrpDetailedSolution
import io.github.pintowar.opta.router.core.domain.models.VrpSolutionRequest
import java.util.UUID

interface SolverEventsPort {

    fun enqueueRequestSolver(command: RequestSolverCommand)

    fun enqueueSolutionRequest(command: SolutionRequestCommand)

    fun broadcastCancelSolver(command: CancelSolverCommand)
}