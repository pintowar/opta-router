package io.github.pintowar.opta.router.adapters.handler

import io.github.pintowar.opta.router.core.domain.ports.BroadcastPort
import io.github.pintowar.opta.router.core.domain.repository.SolverRepository
import io.github.pintowar.opta.router.core.solver.VrpSolverService
import org.springframework.context.event.EventListener

class SpringEventsHandler(
    private val solver: VrpSolverService,
    private val solverRepository: SolverRepository,
    private val broadcastPort: BroadcastPort
) {

    @EventListener
    fun requestSolverListener(requestSolverEvent: RequestSolverEvent) {
        val cmd = requestSolverEvent.command
        solver.solve(cmd.problemId, cmd.uuid, cmd.solverName)
    }

    @EventListener
    fun solutionRegistryListener(solutionRequestEvent: SolutionRequestEvent) {
        val cmd = solutionRequestEvent.command
        val (solRequest, clear) = cmd.solutionRequest to cmd.clear
        val newSolRequest = solverRepository
            .insertNewSolution(solRequest.solution, solRequest.solverKey!!, solRequest.status, clear)
        broadcastPort.broadcastSolution(newSolRequest)
    }
}