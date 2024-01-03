package io.github.pintowar.opta.router.core.domain.messages

import io.github.pintowar.opta.router.core.domain.models.SolverStatus
import io.github.pintowar.opta.router.core.domain.models.VrpDetailedSolution
import io.github.pintowar.opta.router.core.domain.models.VrpSolutionRequest
import java.util.*

data class RequestSolverCommand(val detailedSolution: VrpDetailedSolution, val solverKey: UUID, val solverName: String)

data class CancelSolverCommand(val solverKey: UUID, val currentStatus: SolverStatus, val clear: Boolean = false)

data class SolutionRequestCommand(val solutionRequest: VrpSolutionRequest, val clear: Boolean)

data class SolutionCommand(val solutionRequest: VrpSolutionRequest)