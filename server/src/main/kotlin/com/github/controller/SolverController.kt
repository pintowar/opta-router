package com.github.controller

import com.github.service.VrpSolverService
import com.github.util.GraphWrapper
import com.github.vrp.Instance
import com.github.vrp.SolverState
import mu.KLogging
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*


/**
 * The Controller that contains all REST functions to be used on the application.
 */
@RestController
@RequestMapping("/api/solver")
class SolverController(val solver: VrpSolverService, val graph: GraphWrapper) {

    companion object : KLogging()

    @PostMapping("/{id}/solve", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun solve(@PathVariable id: Long, @RequestBody instance: Instance): SolverState {
        solver.asyncSolve(instance)
        return SolverState(solver.showStatus(id))
    }

//    @GetMapping("/status", produces = [MediaType.APPLICATION_JSON_VALUE])
//    fun status(): SolverState {
//        return SolverState(solver.showStatus(1L), solver.isViewDetailed(1L))
//    }
//
//    @GetMapping("/instance", produces = [MediaType.APPLICATION_JSON_VALUE])
//    fun instance(): Instance {
//        return solver.currentInstance(1L)
//    }
//
//    @GetMapping("/solution", produces = [MediaType.APPLICATION_JSON_VALUE])
//    fun solution(): VrpSolution {
//        val solution = solver.currentSolution(1L)
//        return solution.convertSolution(if (solver.isViewDetailed(1L)) graph else null)
//    }

    @PutMapping("/{id}/detailed-path/{status}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun detailedPath(@PathVariable id: Long, @PathVariable status: Boolean): SolverState {
        solver.updateDetailedView(id, status)
        return SolverState(solver.showStatus(id))
    }

    @GetMapping("{id}/terminate", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun terminateEarly(@PathVariable id: Long,): SolverState {
        solver.terminateEarly(id)
        return SolverState(solver.showStatus(id))
    }

    @GetMapping("{id}/clean", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun clean(@PathVariable id: Long,): SolverState {
        solver.clean(id)
        return SolverState(solver.showStatus(id))
    }
}
