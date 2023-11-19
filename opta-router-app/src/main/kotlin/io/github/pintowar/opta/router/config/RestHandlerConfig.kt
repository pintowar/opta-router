package io.github.pintowar.opta.router.config

import io.github.pintowar.opta.router.core.domain.models.*
import io.github.pintowar.opta.router.core.domain.ports.GeoPort
import io.github.pintowar.opta.router.core.domain.ports.VrpProblemPort
import io.github.pintowar.opta.router.core.domain.ports.VrpSolverRequestPort
import io.github.pintowar.opta.router.core.domain.ports.VrpSolverSolutionPort
import io.github.pintowar.opta.router.core.solver.VrpSolverService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springdoc.core.annotations.RouterOperation
import org.springdoc.core.annotations.RouterOperations
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.web.reactive.function.server.*

@Profile("rest-api")
@Configuration
class RestHandlerConfig {

    data class PanelSolutionState(val solverPanel: SolverPanel, val solutionState: VrpSolutionRequest)

    @Bean
    @RouterOperations(
        RouterOperation(
            path = "/api/solver/solver-names",
            operation = Operation(
                operationId = "solverNames",
                method = "GET",
                tags = ["solver-handler"],
                responses = [
                    ApiResponse(
                        responseCode = "200",
                        description = "OK",
                        content = [Content(array = ArraySchema(schema = Schema(implementation = String::class)))]
                    )
                ]
            )
        ),
        RouterOperation(
            path = "/api/solver/{id}/solve/{solverName}",
            operation = Operation(
                operationId = "solve",
                method = "POST",
                tags = ["solver-handler"],
                parameters = [
                    Parameter(
                        `in` = ParameterIn.PATH,
                        name = "id",
                        required = true,
                        schema = Schema(type = "integer")
                    )
                ],
                responses = [
                    ApiResponse(
                        responseCode = "200",
                        description = "OK",
                        content = [Content(schema = Schema(implementation = SolverStatus::class))]
                    )
                ]
            )
        ),
        RouterOperation(
            path = "/api/solver/{id}/terminate",
            operation = Operation(
                operationId = "terminate",
                method = "POST",
                tags = ["solver-handler"],
                parameters = [
                    Parameter(
                        `in` = ParameterIn.PATH,
                        name = "id",
                        required = true,
                        schema = Schema(type = "integer")
                    )
                ],
                responses = [
                    ApiResponse(
                        responseCode = "200",
                        description = "OK",
                        content = [Content(schema = Schema(implementation = SolverStatus::class))]
                    )
                ]
            )
        ),
        RouterOperation(
            path = "/api/solver/{id}/clean",
            operation = Operation(
                operationId = "clear",
                method = "POST",
                tags = ["solver-handler"],
                parameters = [
                    Parameter(
                        `in` = ParameterIn.PATH,
                        name = "id",
                        required = true,
                        schema = Schema(type = "integer")
                    )
                ],
                responses = [
                    ApiResponse(
                        responseCode = "200",
                        description = "OK",
                        content = [Content(schema = Schema(implementation = SolverStatus::class))]
                    )
                ]
            )
        ),
        RouterOperation(
            path = "/api/solver/{id}/detailed-path/{isDetailed}",
            operation = Operation(
                operationId = "detailedPath",
                method = "PUT",
                tags = ["solver-handler"],
                parameters = [
                    Parameter(
                        `in` = ParameterIn.PATH,
                        name = "id",
                        required = true,
                        schema = Schema(type = "integer")
                    ),
                    Parameter(
                        `in` = ParameterIn.PATH,
                        name = "isDetailed",
                        required = true,
                        schema = Schema(type = "boolean")
                    )
                ],
                responses = [
                    ApiResponse(
                        responseCode = "200",
                        description = "OK",
                        content = [Content(schema = Schema(implementation = SolverStatus::class))]
                    )
                ]
            )
        ),
        RouterOperation(
            path = "/api/solver/{id}/solution-panel",
            operation = Operation(
                operationId = "solutionState",
                method = "GET",
                tags = ["solver-handler"],
                parameters = [
                    Parameter(
                        `in` = ParameterIn.PATH,
                        name = "id",
                        required = true,
                        schema = Schema(type = "integer")
                    )
                ],
                responses = [
                    ApiResponse(
                        responseCode = "200",
                        description = "OK",
                        content = [Content(schema = Schema(implementation = PanelSolutionState::class))]
                    )
                ]
            )
        ),
    )
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
    @RouterOperations(
        RouterOperation(
            path = "/api/vrp-problems",
            operation = Operation(
                operationId = "index",
                method = "GET",
                tags = ["vrp-problem-handler"],
                responses = [
                    ApiResponse(
                        responseCode = "200",
                        description = "OK",
                        content = [Content(array = ArraySchema(schema = Schema(implementation = VrpProblem::class)))]
                    )
                ]
            )
        ),
        RouterOperation(
            path = "/api/vrp-problems/{id}",
            operation = Operation(
                operationId = "show",
                method = "GET",
                tags = ["vrp-problem-handler"],
                parameters = [
                    Parameter(
                        `in` = ParameterIn.PATH,
                        name = "id",
                        required = true,
                        schema = Schema(type = "integer")
                    )
                ],
                responses = [
                    ApiResponse(
                        responseCode = "200",
                        description = "OK",
                        content = [Content(schema = Schema(implementation = VrpProblem::class))]
                    )
                ]
            )
        )
    )
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
    @RouterOperations(
        RouterOperation(
            path = "/api/solver-history/{problemId}/requests/{solverName}",
            operation = Operation(
                operationId = "requests",
                method = "GET",
                tags = ["solver-history-handler"],
                parameters = [
                    Parameter(
                        `in` = ParameterIn.PATH,
                        name = "problemId",
                        required = true,
                        schema = Schema(type = "integer")
                    ),
                    Parameter(
                        `in` = ParameterIn.PATH,
                        name = "solverName",
                        required = true,
                        schema = Schema(type = "string")
                    )
                ],
                responses = [
                    ApiResponse(
                        responseCode = "200",
                        description = "OK",
                        content = [Content(array = ArraySchema(schema = Schema(implementation = VrpSolverRequest::class)))]
                    )
                ]
            )
        ),
        RouterOperation(
            path = "/api/solver-history/{problemId}/solutions",
            operation = Operation(
                operationId = "solutions",
                method = "GET",
                tags = ["solver-history-handler"],
                parameters = [
                    Parameter(
                        `in` = ParameterIn.PATH,
                        name = "problemId",
                        required = true,
                        schema = Schema(type = "integer")
                    )
                ],
                responses = [
                    ApiResponse(
                        responseCode = "200",
                        description = "OK",
                        content = [Content(array = ArraySchema(schema = Schema(implementation = VrpSolverObjective::class)))]
                    )
                ]
            )
        )
    )
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