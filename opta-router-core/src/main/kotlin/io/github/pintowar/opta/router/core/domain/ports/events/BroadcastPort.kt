package io.github.pintowar.opta.router.core.domain.ports.events

import io.github.pintowar.opta.router.core.domain.messages.SolutionCommand

/**
 * The BroadcastPort is responsible for broadcasting solution updates to all interested parties.
 */
interface BroadcastPort {
    /**
     * Broadcasts a solution command.
     *
     * @param command The [SolutionCommand] to broadcast.
     */
    fun broadcastSolution(command: SolutionCommand)
}