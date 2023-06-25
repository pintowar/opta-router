package io.github.pintowar.opta.router.controller

import io.github.pintowar.opta.router.core.domain.models.SolverPanel
import io.github.pintowar.opta.router.core.domain.models.SolverStatus
import io.github.pintowar.opta.router.core.domain.models.VrpSolutionRequest
import io.github.pintowar.opta.router.core.domain.ports.GeoPort
import io.github.pintowar.opta.router.core.solver.VrpSolverService
import jakarta.servlet.http.HttpSession
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * The Controller that contains all REST functions to be used on the application.
 */
@RestController
@RequestMapping("/api/solver")
class SolverController(
    private val solver: VrpSolverService,
    private val geoService: GeoPort,
    private val sessionPanel: MutableMap<String, SolverPanel>
) {

    data class PanelSolutionState(val solverPanel: SolverPanel, val solutionState: VrpSolutionRequest)

    @PostMapping(
        "/{id}/solve",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun solve(@PathVariable id: Long): ResponseEntity<SolverStatus> {
        solver.enqueueSolverRequest(id, "jsprit")
        return ResponseEntity.ok(solver.showStatus(id))
    }

    @PutMapping("/{id}/detailed-path/{isDetailed}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun detailedPath(
        @PathVariable id: Long,
        @PathVariable isDetailed: Boolean,
        session: HttpSession
    ): ResponseEntity<SolverStatus> {
        sessionPanel[session.id] = SolverPanel(isDetailed)
        solver.updateDetailedView(id)
        return ResponseEntity.ok(solver.showStatus(id))
    }

    @GetMapping("/{id}/terminate", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun terminateEarly(@PathVariable id: Long): ResponseEntity<SolverStatus> {
        solver.currentSolutionRequest(id)?.also { it.solverKey?.also(solver::terminateEarly) }
        return ResponseEntity.ok(solver.showStatus(id))
    }

    @GetMapping("/{id}/clean", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun clean(@PathVariable id: Long): ResponseEntity<SolverStatus> {
        solver.currentSolutionRequest(id)?.also { it.solverKey?.also(solver::clean) }
        return ResponseEntity.ok(solver.showStatus(id))
    }

    @GetMapping("/{id}/solution-panel", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun solutionState(@PathVariable id: Long, session: HttpSession): ResponseEntity<PanelSolutionState> {
        val panel = sessionPanel[session.id] ?: SolverPanel()

        return solver.currentSolutionRequest(id)?.let {
            val sol = (if (panel.isDetailedPath) geoService.detailedPaths(it.solution) else it.solution)
            PanelSolutionState(panel, it.copy(solution = sol))
        }?.let { ResponseEntity.ok(it) } ?: ResponseEntity.notFound().build()
    }
}