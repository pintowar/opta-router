package io.github.pintowar.opta.router.controller

import io.github.pintowar.opta.router.service.VrpSolverService
import io.github.pintowar.opta.router.vrp.Instance
import io.github.pintowar.opta.router.vrp.SolverState
import io.github.pintowar.opta.router.vrp.VrpSolutionState
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * The Controller that contains all REST functions to be used on the application.
 */
@RestController
@RequestMapping("/api/solver")
class SolverController(val solver: VrpSolverService) {

    @PostMapping(
        "/{id}/solve",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun solve(@PathVariable id: Long, @RequestBody instance: Instance): ResponseEntity<SolverState> {
        solver.asyncSolve(instance)
        return solver.showState(id)
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()
    }

    @PutMapping("/{id}/detailed-path/{status}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun detailedPath(@PathVariable id: Long, @PathVariable status: Boolean): ResponseEntity<SolverState> {
        solver.updateDetailedView(id, status)
        return solver.showState(id)
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()
    }

    @GetMapping("{id}/terminate", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun terminateEarly(@PathVariable id: Long): ResponseEntity<SolverState> {
        solver.terminateEarly(id)
        return solver.showState(id)
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()
    }

    @GetMapping("{id}/clean", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun clean(@PathVariable id: Long): ResponseEntity<SolverState> {
        solver.clean(id)
        return solver.showState(id)
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()
    }

    @GetMapping("{id}/solution-state", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun solutionState(@PathVariable id: Long): ResponseEntity<VrpSolutionState> {
        return solver.currentSolutionState(id)
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()
    }
}