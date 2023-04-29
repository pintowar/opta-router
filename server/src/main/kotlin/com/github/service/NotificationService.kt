package com.github.service

import com.github.vrp.SolverState
import com.github.vrp.VrpSolution

interface NotificationService {
    fun broadcastSolution(solverState: SolverState?, newBestSolution: VrpSolution)
}