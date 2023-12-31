package io.github.pintowar.opta.router.core.domain.ports

import io.github.pintowar.opta.router.core.domain.models.VrpSolutionRequest

interface BroadcastPort {

    data class SolutionCommand(val solutionRequest: VrpSolutionRequest)

    fun broadcastSolution(command: SolutionCommand)
}