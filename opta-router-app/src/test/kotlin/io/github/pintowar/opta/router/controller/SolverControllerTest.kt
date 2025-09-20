package io.github.pintowar.opta.router.controller

import com.ninjasquad.springmockk.MockkBean
import io.github.pintowar.opta.router.core.domain.models.Fixtures
import io.github.pintowar.opta.router.core.domain.models.SolverPanel
import io.github.pintowar.opta.router.core.domain.models.SolverStatus
import io.github.pintowar.opta.router.core.domain.models.VrpSolution
import io.github.pintowar.opta.router.core.domain.models.VrpSolutionRequest
import io.github.pintowar.opta.router.core.solver.SolverPanelStorage
import io.github.pintowar.opta.router.core.solver.VrpSolverService
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.spring.SpringExtension
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import java.util.UUID

@WebFluxTest(SolverController::class)
class SolverControllerTest : FunSpec() {
    @Autowired
    private lateinit var client: WebTestClient

    @MockkBean
    private lateinit var solverService: VrpSolverService

    @MockkBean
    private lateinit var solverPanelStorage: SolverPanelStorage

    init {
        extensions(SpringExtension())

        beforeEach {
            clearMocks(solverService, solverPanelStorage)
        }

        context("GET") {
            test("/api/solver/solver-names") {
                every { solverService.solverNames() } returns setOf("ortools", "jsprit")

                client
                    .get()
                    .uri("/api/solver/solver-names")
                    .exchange()
                    .expectStatus()
                    .isOk
                    .expectHeader()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .expectBody()
                    .jsonPath("$[0]")
                    .isEqualTo("jsprit")
                    .jsonPath("$[1]")
                    .isEqualTo("ortools")

                coVerify(exactly = 1) { solverService.solverNames() }
            }

            test("/api/solver/:id/solution-panel not found") {
                coEvery { solverService.currentSolutionRequest(any()) } returns null

                client
                    .get()
                    .uri("/api/solver/1/solution-panel")
                    .exchange()
                    .expectStatus()
                    .isNotFound

                coVerify(exactly = 1) { solverService.currentSolutionRequest(any()) }
            }

            test("/api/solver/:id/solution-panel ok") {
                val req = VrpSolutionRequest(Fixtures.solution("sample-4").last(), SolverStatus.ENQUEUED)
                val sol = VrpSolution(req.solution.problem, req.solution.routes)
                val panel = SolverPanel()

                coEvery { solverService.currentSolutionRequest(any()) } returns req
                coEvery { solverPanelStorage.convertSolutionForPanelId(any(), any()) } returns sol
                every { solverPanelStorage.getOrDefault(any()) } returns panel

                client
                    .get()
                    .uri("/api/solver/1/solution-panel")
                    .exchange()
                    .expectStatus()
                    .isOk
                    .expectHeader()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .expectBody()
                    .jsonPath("$.solverPanel.isDetailedPath")
                    .isEqualTo(false)
                    .jsonPath("$.solutionState.status")
                    .isEqualTo("ENQUEUED")

                coVerify(exactly = 1) { solverService.currentSolutionRequest(any()) }
            }
        }

        context("POST") {
            test("/api/solver/:id/solve/:solverName") {
                val uuid = UUID.randomUUID()
                coEvery { solverService.enqueueSolverRequest(any(), any()) } returns uuid
                coEvery { solverService.showStatus(any()) } returns SolverStatus.NOT_SOLVED

                client
                    .post()
                    .uri("/api/solver/1/solve/jspirt")
                    .exchange()
                    .expectStatus()
                    .isOk
                    .expectHeader()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)

                coVerify(exactly = 1) { solverService.enqueueSolverRequest(1, "jspirt") }
                coVerify(exactly = 1) { solverService.showStatus(any()) }
            }

            test("/api/solver/:id/terminate") {
                val (id, uuid) = 1L to UUID.randomUUID()
                val req = VrpSolutionRequest(mockk(), SolverStatus.RUNNING, uuid)

                coEvery { solverService.currentSolutionRequest(eq(id)) } returns req
                coEvery { solverService.terminate(eq(uuid)) } just runs
                coEvery { solverService.showStatus(eq(id)) } returns req.status

                client
                    .post()
                    .uri("/api/solver/$id/terminate")
                    .exchange()
                    .expectStatus()
                    .isOk
                    .expectHeader()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)

                coVerify(exactly = 1) { solverService.currentSolutionRequest(id) }
                coVerify(exactly = 1) { solverService.showStatus(id) }
            }

            test("/api/solver/:id/clean") {
                val (id, uuid) = 1L to UUID.randomUUID()
                val req = VrpSolutionRequest(mockk(), SolverStatus.RUNNING, uuid)

                coEvery { solverService.currentSolutionRequest(eq(id)) } returns req
                coEvery { solverService.clear(eq(uuid)) } just runs
                coEvery { solverService.showStatus(eq(id)) } returns req.status

                client
                    .post()
                    .uri("/api/solver/$id/clean")
                    .exchange()
                    .expectStatus()
                    .isOk
                    .expectHeader()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)

                coVerify(exactly = 1) { solverService.currentSolutionRequest(id) }
                coVerify(exactly = 1) { solverService.showStatus(id) }
            }
        }

        context("PUT") {
            test("/api/solver/:id/detailed-path/:isDetailed") {
                val (id, isDetailed) = 1L to true

                coEvery { solverPanelStorage.store(any(), eq(SolverPanel(isDetailed))) } returns null
                coEvery { solverService.showDetailedPath(eq(id)) } just runs
                coEvery { solverService.showStatus(eq(id)) } returns SolverStatus.RUNNING

                client
                    .put()
                    .uri("/api/solver/$id/detailed-path/$isDetailed")
                    .exchange()
                    .expectStatus()
                    .isOk

                coVerify(exactly = 1) { solverPanelStorage.store(any(), SolverPanel(isDetailed)) }
                coVerify(exactly = 1) { solverService.showDetailedPath(id) }
                coVerify(exactly = 1) { solverService.showStatus(id) }
            }
        }
    }
}