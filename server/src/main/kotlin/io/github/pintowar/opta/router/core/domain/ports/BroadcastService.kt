package io.github.pintowar.opta.router.core.domain.ports

import io.github.pintowar.opta.router.core.domain.models.VrpSolutionState

interface BroadcastService {

    fun broadcastSolution(data: VrpSolutionState)
}