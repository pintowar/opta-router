package com.github.controller

import com.github.opta.VehicleRoutingSolverService
import com.github.util.GraphWrapper
import com.github.vrp.Instance
import com.github.vrp.VrpSolution
import com.github.vrp.convertSolution
import org.optaplanner.examples.vehiclerouting.domain.VehicleRoutingSolution
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest

/**
 * The Controller that contains all REST functions to be used on the application.
 */
@RestController
class ActionController(val solver: VehicleRoutingSolverService, val graph: GraphWrapper) {

    @PostMapping("/solve", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun solve(@RequestBody json: Instance, req: HttpServletRequest): Map<String, String> {
        solver.solve(req.session.id, json)
        return mapOf("status" to solver.showStatus(req.session.id))
    }

    @GetMapping("/status", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun status(req: HttpServletRequest): Map<String, String> {
        return mapOf("status" to solver.showStatus(req.session.id),
                "detailed-path" to solver.isViewDetailed(req.session.id).toString())
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
    fun detailedPath(@PathVariable status: Boolean, req: HttpServletRequest): Map<String, String> {
        solver.changeDetailedView(req.session.id, status)
        return mapOf("status" to solver.showStatus(req.session.id))
    }

    @GetMapping("/terminate", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun terminateEarly(req: HttpServletRequest): Map<String, String> {
        solver.terminateEarly(req.session.id)
        return mapOf("status" to solver.showStatus(req.session.id))
    }

    @GetMapping("/clean", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun clean(req: HttpServletRequest): Map<String, String> {
        solver.clean(req.session.id)
        return mapOf("status" to solver.showStatus(req.session.id))
    }
}

/**
 *  The Controller that will render (an creates the user session) the main page.
 */
@Controller
class HomeController {

    @GetMapping("/")
    fun index(req: HttpServletRequest): String {
        LOGGER.info("Session ID: {}", req.session.id) // Creates the user session.
        return "index.html"
    }

    companion object {
        internal val LOGGER = LoggerFactory.getLogger(HomeController::class.java)
    }
}