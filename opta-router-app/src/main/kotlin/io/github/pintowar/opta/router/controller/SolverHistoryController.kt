package io.github.pintowar.opta.router.controller

import io.github.pintowar.opta.router.config.ConfigData
import io.github.pintowar.opta.router.core.domain.models.VrpSolverObjective
import io.github.pintowar.opta.router.core.domain.models.VrpSolverRequest
import io.github.pintowar.opta.router.core.domain.ports.repo.VrpSolverRequestPort
import io.github.pintowar.opta.router.core.domain.ports.repo.VrpSolverSolutionPort
import kotlinx.coroutines.flow.Flow
import org.springframework.context.annotation.Profile
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * The Controller that contains all REST functions to be used on the application.
 */
@RestController
@Profile(ConfigData.REST_PROFILE)
@RequestMapping("/api/solver-history")
class SolverHistoryController(
    private val vrpSolverRequestPort: VrpSolverRequestPort,
    private val vrpSolverSolutionPort: VrpSolverSolutionPort
) {
    /**
     * Returns a flow of VRP solver requests for a given problem and solver name.
     *
     * @param problemId The ID of the VRP problem.
     * @param solverName The name of the solver.
     * @return A `ResponseEntity` with a flow of VRP solver requests.
     */
    @GetMapping("/{problemId}/requests/{solverName}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun requests(
        @PathVariable problemId: Long,
        @PathVariable solverName: String
    ): ResponseEntity<Flow<VrpSolverRequest>> =
        vrpSolverRequestPort.requestsByProblemIdAndSolverName(problemId, solverName).let {
            ResponseEntity.ok(it)
        }

    /**
     * Returns a flow of VRP solver objectives for a given problem.
     *
     * @param problemId The ID of the VRP problem.
     * @return A `ResponseEntity` with a flow of VRP solver objectives.
     */
    @GetMapping("/{problemId}/solutions", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun solutions(
        @PathVariable problemId: Long
    ): ResponseEntity<Flow<VrpSolverObjective>> =
        vrpSolverSolutionPort.solutionHistory(problemId).let {
            ResponseEntity.ok(it)
        }
}