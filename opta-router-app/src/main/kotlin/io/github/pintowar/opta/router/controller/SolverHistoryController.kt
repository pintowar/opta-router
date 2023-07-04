package io.github.pintowar.opta.router.controller

import io.github.pintowar.opta.router.core.domain.models.VrpSolverObjective
import io.github.pintowar.opta.router.core.domain.models.VrpSolverRequest
import io.github.pintowar.opta.router.core.domain.ports.VrpSolverRequestPort
import io.github.pintowar.opta.router.core.domain.ports.VrpSolverSolutionPort
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

/**
 * The Controller that contains all REST functions to be used on the application.
 */
@RestController
@RequestMapping("/api/solver-history")
class SolverHistoryController(
    private val vrpSolverRequestPort: VrpSolverRequestPort,
    private val vrpSolverSolutionPort: VrpSolverSolutionPort
) {

    @GetMapping("/{problemId}/solver-names", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun solverNames(@PathVariable problemId: Long): ResponseEntity<List<String>> =
        ResponseEntity.ok(vrpSolverRequestPort.solverHistory(problemId))

    @GetMapping("/{problemId}/requests/{solverName}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun requests(
        @PathVariable problemId: Long,
        @PathVariable solverName: String
    ): ResponseEntity<List<VrpSolverRequest>> {
        return vrpSolverRequestPort.requestsByProblemIdAndSolverName(problemId, solverName).let {
            ResponseEntity.ok(it)
        }
    }

    @GetMapping("/{problemId}/solutions/{requestId}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun solutions(
        @PathVariable problemId: Long,
        @PathVariable requestId: UUID
    ): ResponseEntity<List<VrpSolverObjective>> {
        return vrpSolverSolutionPort.solutionHistory(problemId, requestId).let {
            ResponseEntity.ok(it)
        }
    }
}