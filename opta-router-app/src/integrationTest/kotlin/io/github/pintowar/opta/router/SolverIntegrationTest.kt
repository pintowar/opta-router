package io.github.pintowar.opta.router

import com.ninjasquad.springmockk.MockkBean
import io.github.pintowar.opta.router.core.domain.models.SolverStatus
import io.github.pintowar.opta.router.core.domain.ports.events.BroadcastPort
import io.github.pintowar.opta.router.core.domain.ports.events.SolverEventsPort
import io.github.pintowar.opta.router.core.domain.ports.repo.VrpSolverRequestPort
import io.kotest.matchers.collections.shouldContainInOrder
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.returnResult
import java.lang.IllegalStateException

class SolverIntegrationTest : BaseIntegrationTest() {
    @Autowired
    private lateinit var vrpSolverRequestPort: VrpSolverRequestPort

    @MockkBean
    private lateinit var solverEventsPort: SolverEventsPort

    @MockkBean
    private lateinit var broadcastPort: BroadcastPort

    init {

        test("should list solver names") {
            val uri = "/api/solver/solver-names"
            val exchange = client.get().uri(uri).exchange()

            val solvers = exchange.returnResult<List<String>>().responseBody.awaitSingle()

            exchange.expectStatus().isOk
            exchange.expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
            solvers shouldContainInOrder listOf("jenetics", "jsprit", "or-tools", "timefold")
        }

        context("should enqueue request solver") {

            test("should successful enqueue") {
                val id = 4L
                every { solverEventsPort.enqueueRequestSolver(any()) } just runs

                vrpSolverRequestPort.currentSolverRequest(id)?.status shouldBe null

                val uri = "/api/solver/{id}/solve/{solverName}"
                val exchange = client.post().uri(uri, id, "or-tools").exchange()

                val status = exchange.returnResult<String>().responseBody.awaitSingle()
                exchange.expectStatus().isOk
                exchange.expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
                status shouldBe "\"ENQUEUED\""
                vrpSolverRequestPort.currentSolverRequest(id)?.status shouldBe SolverStatus.ENQUEUED
            }

            test("should fail queue") {
                val id = 4L
                every { solverEventsPort.enqueueRequestSolver(any()) } throws
                    IllegalStateException("Unexpected queue state")

                vrpSolverRequestPort.currentSolverRequest(id)?.status shouldBe null

                val uri = "/api/solver/{id}/solve/{solverName}"
                val exchange = client.post().uri(uri, id, "or-tools").exchange()

                val status = exchange.returnResult<String>().responseBody.awaitSingle()
                exchange.expectStatus().isOk
                exchange.expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
                status shouldBe "\"CREATED\""
                vrpSolverRequestPort.currentSolverRequest(id)?.status shouldBe SolverStatus.CREATED
            }
        }
    }
}