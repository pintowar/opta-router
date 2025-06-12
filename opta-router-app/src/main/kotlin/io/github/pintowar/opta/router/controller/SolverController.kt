package io.github.pintowar.opta.router.controller

import io.github.pintowar.opta.router.config.ConfigData
import io.github.pintowar.opta.router.core.domain.models.SolverPanel
import io.github.pintowar.opta.router.core.domain.models.SolverStatus
import io.github.pintowar.opta.router.core.domain.models.VrpSolutionRequest
import io.github.pintowar.opta.router.core.solver.SolverPanelStorage
import io.github.pintowar.opta.router.core.solver.VrpSolverService
import io.swagger.v3.oas.annotations.Parameter
import org.springframework.context.annotation.Profile
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.WebSession

/**
 * The Controller that contains all REST functions to be used on the application.
 */
@RestController
@Profile(ConfigData.REST_PROFILE)
@RequestMapping("/api/solver")
class SolverController(
    private val solverService: VrpSolverService,
    private val solverPanelStorage: SolverPanelStorage
) {
    data class PanelSolutionState(val solverPanel: SolverPanel, val solutionState: VrpSolutionRequest)

    @GetMapping("/solver-names", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun solverNames() = solverService.solverNames().sorted()

    @PostMapping("/{id}/solve/{solverName}", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun solve(
        @PathVariable id: Long,
        @PathVariable solverName: String
    ): ResponseEntity<SolverStatus> {
        solverService.enqueueSolverRequest(id, solverName)
        return ResponseEntity.ok(solverService.showStatus(id))
    }

    @PostMapping("/{id}/terminate", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun terminate(
        @PathVariable id: Long
    ): ResponseEntity<SolverStatus> {
        solverService.currentSolutionRequest(id)?.also { it.solverKey?.also { key -> solverService.terminate(key) } }
        return ResponseEntity.ok(solverService.showStatus(id))
    }

    @PostMapping("/{id}/clean", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun clear(
        @PathVariable id: Long
    ): ResponseEntity<SolverStatus> {
        solverService.currentSolutionRequest(id)?.also { it.solverKey?.also { key -> solverService.clear(key) } }
        return ResponseEntity.ok(solverService.showStatus(id))
    }

    @PutMapping("/{id}/detailed-path/{isDetailed}", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun detailedPath(
        @PathVariable id: Long,
        @PathVariable isDetailed: Boolean,
        @Parameter(hidden = true) session: WebSession
    ): ResponseEntity<SolverStatus> {
        solverPanelStorage.store(session.id, SolverPanel(isDetailed))
        solverService.showDetailedPath(id)
        return ResponseEntity.ok(solverService.showStatus(id))
    }

    @GetMapping("/{id}/solution-panel", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun solutionState(
        @PathVariable id: Long,
        @Parameter(hidden = true) session: WebSession
    ): ResponseEntity<PanelSolutionState> {
        return solverService.currentSolutionRequest(id)?.let {
            val panel = solverPanelStorage.getOrDefault(session.id)
            val sol = solverPanelStorage.convertSolutionForPanelId(session.id, it.solution)
            PanelSolutionState(panel, it.copy(solution = sol))
        }?.let { ResponseEntity.ok(it) } ?: ResponseEntity.notFound().build()
    }
}