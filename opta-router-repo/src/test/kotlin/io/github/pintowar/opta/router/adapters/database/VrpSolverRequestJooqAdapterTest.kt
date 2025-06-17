package io.github.pintowar.opta.router.adapters.database

import io.github.pintowar.opta.router.adapters.database.util.TestUtils
import io.github.pintowar.opta.router.core.domain.models.SolverStatus
import io.github.pintowar.opta.router.core.domain.models.VrpSolverRequest
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.runBlocking
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitSingle
import org.jooq.generated.tables.references.VRP_SOLVER_REQUEST
import java.time.Duration
import java.time.Instant
import java.util.UUID

class VrpSolverRequestJooqAdapterTest : FunSpec({

    coroutineTestScope = true

    val dsl = TestUtils.initDB()
    val adapter = VrpSolverRequestJooqAdapter(dsl)

    beforeSpec {
        runBlocking { TestUtils.cleanTables(dsl) }
    }

    beforeEach {
        runBlocking { TestUtils.runInitScript(dsl) }
    }

    afterEach {
        runBlocking { TestUtils.cleanTables(dsl) }
    }

    test("refreshSolverRequests should update status of old running requests to TERMINATED") {
        val oldRequest = VrpSolverRequest(UUID.randomUUID(), 1L, "solver1", SolverStatus.RUNNING)
        val now = Instant.now()
        dsl.insertInto(VRP_SOLVER_REQUEST)
            .set(VRP_SOLVER_REQUEST.REQUEST_KEY, oldRequest.requestKey)
            .set(VRP_SOLVER_REQUEST.VRP_PROBLEM_ID, oldRequest.problemId)
            .set(VRP_SOLVER_REQUEST.SOLVER, oldRequest.solver)
            .set(VRP_SOLVER_REQUEST.STATUS, oldRequest.status.name)
            .set(VRP_SOLVER_REQUEST.CREATED_AT, now.minus(Duration.ofMinutes(10)))
            .set(VRP_SOLVER_REQUEST.UPDATED_AT, now.minus(Duration.ofMinutes(10)))
            .awaitSingle()

        val updatedCount = adapter.refreshSolverRequests(Duration.ofMinutes(5))
        updatedCount shouldBe 1

        val refreshedRequest = adapter.currentSolverRequest(oldRequest.requestKey)
        refreshedRequest?.status shouldBe SolverStatus.TERMINATED
    }

    test("createRequest should insert a new solver request if no active request exists") {
        val newRequest = VrpSolverRequest(UUID.randomUUID(), 1L, "solver1", SolverStatus.ENQUEUED)
        val createdRequest = adapter.createRequest(newRequest)
        createdRequest shouldBe newRequest

        val foundRequest = adapter.currentSolverRequest(newRequest.requestKey)
        foundRequest shouldBe newRequest
    }

    test("createRequest should not insert a new solver request if an active request exists for the same problem") {
        val existingRequest = VrpSolverRequest(UUID.randomUUID(), 1L, "solver1", SolverStatus.RUNNING)
        adapter.createRequest(existingRequest)

        val newRequest = VrpSolverRequest(UUID.randomUUID(), 1L, "solver2", SolverStatus.ENQUEUED)
        val createdRequest = adapter.createRequest(newRequest)
        createdRequest shouldBe null
    }

    test("currentSolverRequest by problemId should return the latest request") {
        val problemId = 2L
        val request1 = VrpSolverRequest(UUID.randomUUID(), problemId, "solverA", SolverStatus.ENQUEUED)
        val request2 = VrpSolverRequest(UUID.randomUUID(), problemId, "solverB", SolverStatus.RUNNING)

        adapter.createRequest(request1)
        // Simulate a slight delay for updated_at to be different
        Thread.sleep(100)
        dsl.insertInto(VRP_SOLVER_REQUEST)
            .set(VRP_SOLVER_REQUEST.REQUEST_KEY, request2.requestKey)
            .set(VRP_SOLVER_REQUEST.VRP_PROBLEM_ID, request2.problemId)
            .set(VRP_SOLVER_REQUEST.SOLVER, request2.solver)
            .set(VRP_SOLVER_REQUEST.STATUS, request2.status.name)
            .set(VRP_SOLVER_REQUEST.CREATED_AT, Instant.now())
            .set(VRP_SOLVER_REQUEST.UPDATED_AT, Instant.now())
            .awaitSingle()

        val currentRequest = adapter.currentSolverRequest(problemId)
        currentRequest?.requestKey shouldBe request2.requestKey
    }

    test("currentSolverRequest by solverKey should return the specific request") {
        val request = VrpSolverRequest(UUID.randomUUID(), 3L, "solverC", SolverStatus.TERMINATED)
        adapter.createRequest(request)

        val foundRequest = adapter.currentSolverRequest(request.requestKey)
        foundRequest shouldBe request
    }

    test("requestsByProblemIdAndSolverName should return all requests for a given problem and solver") {
        val problemId = 4L
        val solverName = "solverD"
        val request1 = VrpSolverRequest(UUID.randomUUID(), problemId, solverName, SolverStatus.ENQUEUED)
        val request2 = VrpSolverRequest(UUID.randomUUID(), problemId, solverName, SolverStatus.RUNNING)
        val request3 = VrpSolverRequest(UUID.randomUUID(), problemId, "anotherSolver", SolverStatus.ENQUEUED)

        adapter.createRequest(request1)
        adapter.createRequest(request2)
        adapter.createRequest(request3) // This one should not be returned

        val requests = adapter.requestsByProblemIdAndSolverName(problemId, solverName).toList()
        requests.size shouldBe 1
        requests.map { it.requestKey } shouldBe listOf(request1.requestKey)
    }
})
