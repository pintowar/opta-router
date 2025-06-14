package io.github.pintowar.opta.router.core.solver

import io.github.pintowar.opta.router.core.domain.messages.CancelSolverCommand
import io.github.pintowar.opta.router.core.domain.messages.RequestSolverCommand
import io.github.pintowar.opta.router.core.domain.messages.SolutionCommand
import io.github.pintowar.opta.router.core.domain.messages.SolutionRequestCommand
import io.github.pintowar.opta.router.core.domain.models.Fixtures
import io.github.pintowar.opta.router.core.domain.models.SolverStatus
import io.github.pintowar.opta.router.core.domain.models.VrpDetailedSolution
import io.github.pintowar.opta.router.core.domain.models.VrpSolution
import io.github.pintowar.opta.router.core.domain.models.VrpSolutionRequest
import io.github.pintowar.opta.router.core.domain.models.VrpSolverRequest
import io.github.pintowar.opta.router.core.domain.ports.events.BroadcastPort
import io.github.pintowar.opta.router.core.domain.ports.events.SolverEventsPort
import io.github.pintowar.opta.router.core.domain.repository.SolverRepository
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertNull
import java.util.*

class VrpSolverServiceTest : FunSpec({
    val solverRepository: SolverRepository = mockk()
    val broadcastPort: BroadcastPort = mockk(relaxed = true)
    val solverEventsPort: SolverEventsPort = mockk(relaxed = true)

    val vrpSolverService = VrpSolverService(broadcastPort, solverEventsPort, solverRepository)

    val problemId = 1L
    val solverKey = UUID.randomUUID()
    val requestKey = UUID.randomUUID()
    val solverName = "testSolver"

    val sampleProblem = Fixtures.problem("sample-4")
    val runningSolverRequest =
        VrpSolverRequest(
            requestKey,
            sampleProblem.id,
            "solver-name",
            SolverStatus.RUNNING
        )

    afterTest {
        clearMocks(solverRepository, broadcastPort, solverEventsPort)
    }

    test("currentSolutionRequest should return solution request when repository returns one") {
        val emptySol = VrpSolution.emptyFromInstance(sampleProblem)
        val solutionRequest = VrpSolutionRequest(emptySol, SolverStatus.NOT_SOLVED, solverKey)
        coEvery { solverRepository.currentSolutionRequest(problemId) } returns solutionRequest

        val result = vrpSolverService.currentSolutionRequest(problemId)

        result shouldBe solutionRequest
        coVerify(exactly = 1) { solverRepository.currentSolutionRequest(problemId) }
    }

    test("currentSolutionRequest should return null when repository returns null") {
        coEvery { solverRepository.currentSolutionRequest(problemId) } returns null

        val result = vrpSolverService.currentSolutionRequest(problemId)

        assertNull(result)
        coVerify(exactly = 1) { solverRepository.currentSolutionRequest(problemId) }
    }

    test("showStatus should return status when repository returns a request") {
        val vrpRequest = runningSolverRequest
        coEvery { solverRepository.currentSolverRequest(problemId) } returns vrpRequest

        val result = vrpSolverService.showStatus(problemId)

        result shouldBe SolverStatus.RUNNING
        coVerify(exactly = 1) { solverRepository.currentSolverRequest(problemId) }
    }

    test("showStatus should return NOT_SOLVED when repository returns null") {
        coEvery { solverRepository.currentSolverRequest(problemId) } returns null

        val result = vrpSolverService.showStatus(problemId)

        result shouldBe SolverStatus.NOT_SOLVED
        coVerify(exactly = 1) { solverRepository.currentSolverRequest(problemId) }
    }

    test("showDetailedPath should broadcast solution when repository returns a solution request") {
        val emptySol = VrpSolution.emptyFromInstance(sampleProblem)
        val solutionRequest = VrpSolutionRequest(emptySol, SolverStatus.RUNNING, solverKey)
        coEvery { solverRepository.currentSolutionRequest(problemId) } returns solutionRequest
        coEvery { broadcastPort.broadcastSolution(any()) } returns Unit

        vrpSolverService.showDetailedPath(problemId)

        coVerify(exactly = 1) { solverRepository.currentSolutionRequest(problemId) }
        coVerify(exactly = 1) { broadcastPort.broadcastSolution(SolutionCommand(solutionRequest)) }
    }

    test("showDetailedPath should not broadcast solution when repository returns null") {
        coEvery { solverRepository.currentSolutionRequest(problemId) } returns null

        vrpSolverService.showDetailedPath(problemId)

        coVerify(exactly = 1) { solverRepository.currentSolutionRequest(problemId) }
        coVerify(exactly = 0) { broadcastPort.broadcastSolution(any()) }
    }

    test("update should call addNewSolution on repository") {
        val solution = VrpSolution.emptyFromInstance(sampleProblem)
        val status = SolverStatus.RUNNING
        val clear = true
        val solRequest = VrpSolutionRequest(solution, status, solverKey)

        coEvery {
            solverRepository.addNewSolution(solution, solverKey, status, clear)
        } returns solRequest

        val result = vrpSolverService.update(solRequest, clear)

        result shouldBe solRequest
        coVerify(exactly = 1) {
            solverRepository.addNewSolution(solution, solverKey, status, clear)
        }
    }

    test(
        """
        enqueueSolverRequest should enqueue event and return request key when repository returns request and 
        detailed solution
        """.trimIndent()
    ) {
        val vrpRequest = runningSolverRequest.copy(status = SolverStatus.ENQUEUED)
        val emptySolution = VrpSolution.emptyFromInstance(sampleProblem)
        val detailedSolution = VrpDetailedSolution(emptySolution, mockk())

        coEvery { solverRepository.enqueue(problemId, solverName) } returns vrpRequest
        coEvery { solverRepository.currentDetailedSolution(problemId) } returns detailedSolution
        coEvery { solverEventsPort.enqueueRequestSolver(any()) } returns Unit

        val result = vrpSolverService.enqueueSolverRequest(problemId, solverName)

        result shouldBe requestKey
        coVerify(exactly = 1) { solverRepository.enqueue(problemId, solverName) }
        coVerify(exactly = 1) { solverRepository.currentDetailedSolution(problemId) }
        coVerify(exactly = 1) {
            solverEventsPort.enqueueRequestSolver(
                RequestSolverCommand(
                    detailedSolution,
                    requestKey,
                    solverName
                )
            )
        }
    }

    test("enqueueSolverRequest should return null and not enqueue event when repository enqueue returns null") {
        coEvery { solverRepository.enqueue(problemId, solverName) } returns null

        val result = vrpSolverService.enqueueSolverRequest(problemId, solverName)

        assertNull(result)
        coVerify(exactly = 1) { solverRepository.enqueue(problemId, solverName) }
        coVerify(exactly = 0) { solverRepository.currentDetailedSolution(problemId) }
        coVerify(exactly = 0) { solverEventsPort.enqueueRequestSolver(any()) }
    }

    test(
        """
        enqueueSolverRequest should return null and not enqueue event when repository currentDetailedSolution 
        returns null
        """.trimIndent()
    ) {
        val vrpRequest = runningSolverRequest.copy(status = SolverStatus.ENQUEUED)
        coEvery { solverRepository.enqueue(problemId, solverName) } returns vrpRequest
        coEvery { solverRepository.currentDetailedSolution(problemId) } returns null

        val result = vrpSolverService.enqueueSolverRequest(problemId, solverName)

        assertNull(result)
        coVerify(exactly = 1) { solverRepository.enqueue(problemId, solverName) }
        coVerify(exactly = 1) { solverRepository.currentDetailedSolution(problemId) }
        coVerify(exactly = 0) { solverEventsPort.enqueueRequestSolver(any()) }
    }

    context("terminate and clear") {

        test("terminate should broadcast cancel command when solver is RUNNING") {
            val solverRequest = runningSolverRequest
            coEvery { solverRepository.currentSolverRequest(requestKey) } returns solverRequest
            coJustRun { solverEventsPort.broadcastCancelSolver(any()) }

            vrpSolverService.terminate(requestKey)

            coVerify(exactly = 1) {
                solverEventsPort.broadcastCancelSolver(
                    CancelSolverCommand(requestKey, SolverStatus.RUNNING, false)
                )
            }
            coVerify(exactly = 0) { solverEventsPort.enqueueSolutionRequest(any()) } // Ensure no other calls
        }

        test("terminate should broadcast cancel command when solver is ENQUEUED") {
            val solverRequest = runningSolverRequest.copy(status = SolverStatus.ENQUEUED)
            coEvery { solverRepository.currentSolverRequest(requestKey) } returns solverRequest
            coJustRun { solverEventsPort.broadcastCancelSolver(any()) }

            vrpSolverService.terminate(requestKey)

            coVerify(exactly = 1) {
                solverEventsPort.broadcastCancelSolver(
                    CancelSolverCommand(requestKey, SolverStatus.ENQUEUED, false)
                )
            }
            coVerify(exactly = 0) { solverEventsPort.enqueueSolutionRequest(any()) } // Ensure no other calls
        }

        test("terminate should do nothing when solver is TERMINATED") {
            val solverRequest = runningSolverRequest.copy(status = SolverStatus.TERMINATED)
            coEvery { solverRepository.currentSolverRequest(requestKey) } returns solverRequest
            coJustRun { solverEventsPort.broadcastCancelSolver(any()) }
            coJustRun { solverEventsPort.enqueueSolutionRequest(any()) }

            vrpSolverService.terminate(requestKey)

            coVerify(exactly = 0) { solverEventsPort.broadcastCancelSolver(any()) }
            coVerify(exactly = 0) { solverEventsPort.enqueueSolutionRequest(any()) }
        }

        test("terminate should do nothing when solver is not found") {
            coEvery { solverRepository.currentSolverRequest(requestKey) } returns null

            coJustRun { solverEventsPort.broadcastCancelSolver(any()) }
            coJustRun { solverEventsPort.enqueueSolutionRequest(any()) }

            vrpSolverService.terminate(requestKey)

            coVerify(exactly = 0) { solverEventsPort.broadcastCancelSolver(any()) }
            coVerify(exactly = 0) { solverEventsPort.enqueueSolutionRequest(any()) }
        }

        test("clear should broadcast cancel command when solver is RUNNING") {
            val solverRequest = runningSolverRequest
            coEvery { solverRepository.currentSolverRequest(requestKey) } returns solverRequest
            coJustRun { solverEventsPort.broadcastCancelSolver(any()) }

            vrpSolverService.clear(requestKey)

            coVerify(exactly = 1) {
                solverEventsPort.broadcastCancelSolver(
                    CancelSolverCommand(requestKey, SolverStatus.RUNNING, true)
                )
            }
            coVerify(exactly = 0) { solverEventsPort.enqueueSolutionRequest(any()) }
        }

        test("clear should broadcast cancel command when solver is ENQUEUED") {
            val solverRequest = runningSolverRequest.copy(status = SolverStatus.ENQUEUED)
            coEvery { solverRepository.currentSolverRequest(requestKey) } returns solverRequest
            coJustRun { solverEventsPort.broadcastCancelSolver(any()) }

            vrpSolverService.clear(requestKey)

            coVerify(exactly = 1) {
                solverEventsPort.broadcastCancelSolver(
                    CancelSolverCommand(requestKey, SolverStatus.ENQUEUED, true)
                )
            }
            coVerify(exactly = 0) { solverEventsPort.enqueueSolutionRequest(any()) } // Ensure no other calls
        }

        test("clear should enqueue solution request when solver is TERMINATED and solution request exists") {
            val solverRequest = runningSolverRequest.copy(status = SolverStatus.TERMINATED)
            val emptySol = VrpSolution.emptyFromInstance(sampleProblem)
            val solutionRequest = VrpSolutionRequest(emptySol, SolverStatus.TERMINATED, solverKey)

            coEvery { solverRepository.currentSolverRequest(requestKey) } returns solverRequest
            coEvery { solverRepository.currentSolutionRequest(sampleProblem.id) } returns solutionRequest
            coJustRun { solverEventsPort.enqueueSolutionRequest(any()) }

            vrpSolverService.clear(requestKey)

            coVerify(exactly = 1) {
                solverEventsPort.enqueueSolutionRequest(
                    SolutionRequestCommand(solutionRequest, true)
                )
            }
            coVerify(exactly = 0) { solverEventsPort.broadcastCancelSolver(any()) }
        }

        test("clear should do nothing when solver is TERMINATED but solution request does not exist") {
            val solverRequest = runningSolverRequest.copy(status = SolverStatus.TERMINATED)
            coEvery { solverRepository.currentSolverRequest(requestKey) } returns solverRequest
            coEvery { solverRepository.currentSolutionRequest(sampleProblem.id) } returns null

            coJustRun { solverEventsPort.broadcastCancelSolver(any()) }
            coJustRun { solverEventsPort.enqueueSolutionRequest(any()) }

            vrpSolverService.clear(requestKey)

            coVerify(exactly = 0) { solverEventsPort.broadcastCancelSolver(any()) }
            coVerify(exactly = 0) { solverEventsPort.enqueueSolutionRequest(any()) }
        }

        test("clear should do nothing when solver is not found") {
            coEvery { solverRepository.currentSolverRequest(requestKey) } returns null

            coJustRun { solverEventsPort.broadcastCancelSolver(any()) }
            coJustRun { solverEventsPort.enqueueSolutionRequest(any()) }

            vrpSolverService.clear(requestKey)

            coVerify(exactly = 0) { solverEventsPort.broadcastCancelSolver(any()) }
            coVerify(exactly = 0) { solverEventsPort.enqueueSolutionRequest(any()) }
        }
    }
})