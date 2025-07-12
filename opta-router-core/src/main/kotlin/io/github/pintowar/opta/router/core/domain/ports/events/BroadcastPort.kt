package io.github.pintowar.opta.router.core.domain.ports.events

import io.github.pintowar.opta.router.core.domain.messages.SolutionCommand

fun interface BroadcastPort {
    fun broadcastSolution(command: SolutionCommand)
}