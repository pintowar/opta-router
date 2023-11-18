package io.github.pintowar.opta.router.config

import io.github.pintowar.opta.router.core.domain.models.SolverPanel
import io.github.pintowar.opta.router.core.domain.models.VrpSolutionRequest
import io.github.pintowar.opta.router.core.domain.ports.GeoPort
import io.github.pintowar.opta.router.core.domain.ports.VrpProblemPort
import io.github.pintowar.opta.router.core.domain.ports.VrpSolverRequestPort
import io.github.pintowar.opta.router.core.domain.ports.VrpSolverSolutionPort
import io.github.pintowar.opta.router.core.solver.VrpSolverService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.web.reactive.function.server.*

@Profile("rest-api")
@Configuration
class RestHandlerConfig {

    data class PanelSolutionState(val solverPanel: SolverPanel, val solutionState: VrpSolutionRequest)

    @Bean
    fun solverRouter(
        solverService: VrpSolverService,
        geoService: GeoPort,
        sessionPanel: MutableMap<String, SolverPanel>
    ) = coRouter {
        "/api/solver".nest {
            GET("/solver-names") {
                ok().bodyValueAndAwait(solverService.solverNames().sorted())
            }

            POST("/{id}/solve/{solverName}") { req ->
                val (id, solverName) = req.pathVariable("id").toLong() to req.pathVariable("solverName")
                solverService.enqueueSolverRequest(id, solverName).let {
                    ok().bodyValueAndAwait(solverService.showStatus(id))
                }
            }

            POST("/{id}/terminate") { req ->
                val id = req.pathVariable("id").toLong()
                solverService.currentSolutionRequest(id)?.also {
                    it.solverKey?.also { key -> solverService.terminate(key) }
                }
                ok().bodyValueAndAwait(solverService.showStatus(id))
            }

            POST("/{id}/clean") { req ->
                val id = req.pathVariable("id").toLong()
                solverService.currentSolutionRequest(id)?.also {
                    it.solverKey?.also { key -> solverService.clear(key) }
                }
                ok().bodyValueAndAwait(solverService.showStatus(id))
            }

            PUT("/{id}/detailed-path/{isDetailed}") { req ->
                val (id, isDetailed) = req.pathVariable("id").toLong() to req.pathVariable("isDetailed").toBoolean()
                sessionPanel[req.awaitSession().id] = SolverPanel(isDetailed)
                solverService.updateDetailedView(id)
                ok().bodyValueAndAwait(solverService.showStatus(id))
            }

            GET("/{id}/solution-panel") { req ->
                val panel = sessionPanel[req.awaitSession().id] ?: SolverPanel()

                solverService.currentSolutionRequest(req.pathVariable("id").toLong())?.let {
                    val sol = (if (panel.isDetailedPath) geoService.detailedPaths(it.solution) else it.solution)
                    PanelSolutionState(panel, it.copy(solution = sol))
                }?.let { ok().bodyValueAndAwait(it) } ?: notFound().buildAndAwait()
            }
        }
    }

    @Bean
    fun problemRouter(repo: VrpProblemPort) = coRouter {
        "/api/vrp-problems".nest {
            GET("") { ok().bodyAndAwait(repo.listAll()) }

            GET("/{id}") { req ->
                repo.getById(req.pathVariable("id").toLong())
                    ?.let { ok().bodyValueAndAwait(it) }
                    ?: notFound().buildAndAwait()
            }
        }
    }

    @Bean
    fun solverHistoryRouter(
        vrpSolverRequestPort: VrpSolverRequestPort,
        vrpSolverSolutionPort: VrpSolverSolutionPort
    ) = coRouter {
        "/api/solver-history".nest {
            GET("/{problemId}/requests/{solverName}") { req ->
                vrpSolverRequestPort.requestsByProblemIdAndSolverName(
                    req.pathVariable("problemId").toLong(), req.pathVariable("solverName")
                ).let { ok().bodyAndAwait(it) }
            }

            GET("/{problemId}/solutions") { req ->
                vrpSolverSolutionPort.solutionHistory(req.pathVariable("problemId").toLong()).let {
                    ok().bodyAndAwait(it)
                }
            }
        }
    }
}