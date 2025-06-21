package io.github.pintowar.opta.router.adapters.database

import io.github.pintowar.opta.router.adapters.database.util.TestUtils
import io.github.pintowar.opta.router.core.domain.models.Fixtures
import io.github.pintowar.opta.router.core.domain.models.SolverStatus
import io.github.pintowar.opta.router.core.serialization.Serde
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.runBlocking
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitSingle
import org.jooq.generated.tables.references.VRP_SOLVER_REQUEST
import java.time.Instant
import java.util.*

class VrpSolverSolutionJooqAdapterTest :
    FunSpec({

        coroutineTestScope = true

        val serde: Serde = TestUtils.serde()

        val dsl = TestUtils.initDB()
        val adapter = VrpSolverSolutionJooqAdapter(serde, dsl)

        beforeSpec {
            runBlocking { TestUtils.cleanTables(dsl) }
        }

        beforeEach {
            runBlocking { TestUtils.runInitScript(dsl) }
        }

        afterEach {
            runBlocking { TestUtils.cleanTables(dsl) }
        }

        suspend fun createProblemAndRequest(
            problemId: Long,
            requestKey: UUID,
            solverStatus: SolverStatus
        ) {
            val now = Instant.now()

            dsl
                .insertInto(VRP_SOLVER_REQUEST)
                .set(VRP_SOLVER_REQUEST.REQUEST_KEY, requestKey)
                .set(VRP_SOLVER_REQUEST.VRP_PROBLEM_ID, problemId)
                .set(VRP_SOLVER_REQUEST.SOLVER, "test-solver")
                .set(VRP_SOLVER_REQUEST.STATUS, solverStatus.name)
                .set(VRP_SOLVER_REQUEST.CREATED_AT, now)
                .set(VRP_SOLVER_REQUEST.UPDATED_AT, now)
                .awaitSingle()
        }

        test("currentSolution should return empty list if no solution exists") {
            val solution = adapter.currentSolution(1L)
            solution shouldBe emptyList()
        }

        test("currentSolutionRequest should return null if no request exists") {
            val request = adapter.currentSolutionRequest(999L)
            request shouldBe null
        }

        test("upsertSolution should insert a new solution if none exists") {
            val vrpSolution = Fixtures.solution("sample-4").last()
            val routes = vrpSolution.routes
            val problemId = vrpSolution.problem.id
            val requestKey = UUID.randomUUID()
            createProblemAndRequest(problemId, requestKey, SolverStatus.RUNNING)

            val objective = vrpSolution.getTotalDistance().toDouble()
            val solutionRequest =
                adapter.upsertSolution(
                    problemId,
                    SolverStatus.TERMINATED,
                    routes,
                    objective,
                    false,
                    requestKey
                )

            solutionRequest.solution.routes shouldBe routes
            solutionRequest.status shouldBe SolverStatus.TERMINATED

            val currentSolution = adapter.currentSolution(problemId)
            currentSolution shouldBe routes
        }

        test("upsertSolution should update an existing solution") {
            val (firstVrpSolution, lastVrpSolution) = Fixtures.solution("sample-4")
            val problemId = firstVrpSolution.problem.id
            val requestKey = UUID.randomUUID()
            createProblemAndRequest(problemId, requestKey, SolverStatus.RUNNING)

            val initialRoutes = firstVrpSolution.routes
            val initialObjective = firstVrpSolution.getTotalDistance().toDouble()
            adapter.upsertSolution(problemId, SolverStatus.RUNNING, initialRoutes, initialObjective, false, requestKey)

            val updatedRoutes = lastVrpSolution.routes
            val updatedObjective = lastVrpSolution.getTotalDistance().toDouble()
            val solutionRequest =
                adapter.upsertSolution(
                    problemId,
                    SolverStatus.TERMINATED,
                    updatedRoutes,
                    updatedObjective,
                    false,
                    requestKey
                )

            solutionRequest.solution.routes shouldBe updatedRoutes
            solutionRequest.status shouldBe SolverStatus.TERMINATED

            val currentSolution = adapter.currentSolution(problemId)
            currentSolution shouldBe updatedRoutes
        }

        test("upsertSolution should clear solution and set status to NOT_SOLVED if clear is true") {
            val (firstVrpSolution, lastVrpSolution) = Fixtures.solution("sample-4")
            val problemId = firstVrpSolution.problem.id
            val requestKey = UUID.randomUUID()
            createProblemAndRequest(problemId, requestKey, SolverStatus.RUNNING)

            val initialRoutes = firstVrpSolution.routes
            val initialObjective = firstVrpSolution.getTotalDistance().toDouble()
            adapter.upsertSolution(problemId, SolverStatus.RUNNING, initialRoutes, initialObjective, false, requestKey)

            val updatedRoutes = lastVrpSolution.routes
            val updatedObjective = lastVrpSolution.getTotalDistance().toDouble()
            val solutionRequest =
                adapter.upsertSolution(
                    problemId,
                    SolverStatus.NOT_SOLVED,
                    updatedRoutes,
                    updatedObjective,
                    true,
                    requestKey
                )

            solutionRequest.solution.routes shouldBe emptyList()
            solutionRequest.status shouldBe SolverStatus.NOT_SOLVED

            val currentSolution = adapter.currentSolution(problemId)
            currentSolution shouldBe emptyList()
        }

        test("solutionHistory should return a flow of solver objectives") {
            val (firstVrpSolution, lastVrpSolution) = Fixtures.solution("sample-4")
            val problemId = firstVrpSolution.problem.id
            val requestKey1 = UUID.randomUUID()
            val requestKey2 = UUID.randomUUID()
            createProblemAndRequest(problemId, requestKey1, SolverStatus.RUNNING)
            createProblemAndRequest(problemId, requestKey2, SolverStatus.RUNNING)

            val routes1 = firstVrpSolution.routes
            val objective1 = firstVrpSolution.getTotalDistance().toDouble()
            val routes2 = lastVrpSolution.routes
            val objective2 = lastVrpSolution.getTotalDistance().toDouble()

            adapter.upsertSolution(problemId, SolverStatus.TERMINATED, routes1, objective1, false, requestKey1)
            adapter.upsertSolution(problemId, SolverStatus.TERMINATED, routes2, objective2, false, requestKey2)

            val history = adapter.solutionHistory(problemId).toList()
            history.size shouldBe 2
            history.first().objective shouldBe objective1
            history.last().objective shouldBe objective2
            history.first().solverKey shouldBe requestKey1
            history.last().solverKey shouldBe requestKey2
        }
    })