package com.github.service

import com.github.vrp.VrpSolutionState

interface NotificationService {
    fun broadcastSolution(data: VrpSolutionState)
}