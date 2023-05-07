package io.github.pintowar.opta.router.core.domain.ports

import io.github.pintowar.opta.router.core.domain.models.VrpSolutionRegistry

interface BroadcastService {

    fun broadcastSolution(data: VrpSolutionRegistry)
}