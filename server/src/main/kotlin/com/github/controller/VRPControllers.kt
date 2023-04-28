package com.github.controller

import com.github.opta.VehicleRoutingSolverService
import com.github.util.GraphWrapper
import com.github.vrp.Instance
import com.github.vrp.Status
import com.github.vrp.VrpSolution
import com.github.vrp.convertSolution
import jakarta.servlet.http.HttpServletRequest
import mu.KLogging
import org.optaplanner.examples.vehiclerouting.domain.VehicleRoutingSolution
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*


/**
 * The Controller that contains all REST functions to be used on the application.
 */
@RestController
@RequestMapping("/api")
class ActionController(val solver: VehicleRoutingSolverService, val graph: GraphWrapper) {

    companion object : KLogging()
    @GetMapping("/session-id", produces = [MediaType.TEXT_PLAIN_VALUE])
    fun sessionId(req: HttpServletRequest): String? {
        logger.info("Session ID: {}", req.session.id) // Creates the user session.
        return req.session.id
    }

    @PostMapping("/solve", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun solve(@RequestBody json: Instance, req: HttpServletRequest): Status {
        solver.solve(req.session.id, json)
        return Status(solver.showStatus(req.session.id))
    }

    @GetMapping("/status", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun status(req: HttpServletRequest): Status {
        return Status(solver.showStatus(req.session.id), solver.isViewDetailed(req.session.id))
    }

    @GetMapping("/instance", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun instance(req: HttpServletRequest): Instance? {
        return solver.currentInstance(req.session.id)
    }

    @GetMapping("/solution", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun solution(req: HttpServletRequest): VrpSolution {
        val solution = solver.retrieveOrCreateSolution(req.session.id)
        return (solution
                ?: VehicleRoutingSolution()).convertSolution(if (solver.isViewDetailed(req.session.id)) graph else null)
    }

    @PutMapping("/detailed-path/{status}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun detailedPath(@PathVariable status: Boolean, req: HttpServletRequest): Status {
        solver.changeDetailedView(req.session.id, status)
        return Status(solver.showStatus(req.session.id))
    }

    @GetMapping("/terminate", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun terminateEarly(req: HttpServletRequest): Status {
        solver.terminateEarly(req.session.id)
        return Status(solver.showStatus(req.session.id))
    }

    @GetMapping("/clean", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun clean(req: HttpServletRequest): Status {
        solver.clean(req.session.id)
        return Status(solver.showStatus(req.session.id))
    }
}
