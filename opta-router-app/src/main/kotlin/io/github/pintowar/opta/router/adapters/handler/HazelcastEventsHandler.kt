package io.github.pintowar.opta.router.adapters.handler

import io.github.pintowar.opta.router.config.hz.HazelcastEventsRegistry
import io.github.pintowar.opta.router.core.solver.VrpSolverManager
import io.github.pintowar.opta.router.core.solver.VrpSolverService
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component

@Component
class HazelcastEventsHandler(
    private val vrpSolverService: VrpSolverService,
    private val vrpSolverManager: VrpSolverManager,
    private val webSocketHandler: WebSocketHandler,
    hazelcastEventsRegistry: HazelcastEventsRegistry
) {

    init {
        hazelcastEventsRegistry.addBroadcastSolution { webSocketHandler.broadcast(it.solutionRequest) }
        hazelcastEventsRegistry.addSolutionRequestListener {
            runBlocking {
                vrpSolverService.updateAndBroadcast(it.solutionRequest, it.clear)
            }
        }

        hazelcastEventsRegistry.addRequestSolverListener {
            vrpSolverManager.solve(it.solverKey, it.detailedSolution, it.solverName)
        }
        hazelcastEventsRegistry.addBroadcastCancelListener {
            vrpSolverManager.cancelSolver(it.solverKey, it.currentStatus, it.clear)
        }
    }
}