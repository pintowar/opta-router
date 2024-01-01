package io.github.pintowar.opta.router.core.domain.ports

import io.github.pintowar.opta.router.core.domain.messages.SolutionCommand

interface BroadcastPort {

    fun broadcastSolution(command: SolutionCommand)
}