package io.github.pintowar.opta.router.core.domain.ports

import io.github.pintowar.opta.router.core.domain.models.VrpSolutionRequest

interface BroadcastPort {

    fun broadcastSolution(data: VrpSolutionRequest)
}