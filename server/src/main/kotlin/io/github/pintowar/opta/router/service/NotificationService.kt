package io.github.pintowar.opta.router.service

import io.github.pintowar.opta.router.vrp.VrpSolutionState

interface NotificationService {
    fun broadcastSolution(data: VrpSolutionState)
}