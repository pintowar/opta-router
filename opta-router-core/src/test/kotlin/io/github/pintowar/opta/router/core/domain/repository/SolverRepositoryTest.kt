package io.github.pintowar.opta.router.core.domain.repository

import io.github.pintowar.opta.router.core.domain.models.SolverStatus
import io.github.pintowar.opta.router.core.domain.models.VrpDetailedSolution
import io.github.pintowar.opta.router.core.domain.models.VrpSolution
import io.github.pintowar.opta.router.core.domain.models.VrpSolutionRequest
import io.github.pintowar.opta.router.core.domain.models.matrix.VrpProblemMatrix
import io.github.pintowar.opta.router.core.domain.ports.repo.VrpProblemPort
import io.github.pintowar.opta.router.core.domain.ports.repo.VrpSolverRequestPort
import io.github.pintowar.opta.router.core.domain.ports.repo.VrpSolverSolutionPort
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertNull

class SolverRepositoryTest : FunSpec({
    val vrpProblemPort: VrpProblemPort = mockk()
    val vrpSolverSolutionPort: VrpSolverSolutionPort = mockk()
    val vrpSolverRequestPort: VrpSolverRequestPort = mockk()
    val solverRepository = SolverRepository(vrpProblemPort, vrpSolverSolutionPort, vrpSolverRequestPort)

    afterTest {
        clearMocks(vrpProblemPort, vrpSolverSolutionPort, vrpSolverRequestPort)
    }

    test("currentDetailedSolution should return VrpDetailedSolution when matrix and solution exist") {
        val problemId = 1L
        val mockMatrix = mockk<VrpProblemMatrix>(relaxed = true)
        val mockSolution = mockk<VrpSolution>(relaxed = true)
        val mockSolutionRequest = VrpSolutionRequest(mockSolution, SolverStatus.RUNNING)

        coEvery { vrpProblemPort.getMatrixById(problemId) } returns mockMatrix
        coEvery { vrpSolverSolutionPort.currentSolutionRequest(problemId) } returns mockSolutionRequest

        val result = solverRepository.currentDetailedSolution(problemId)

        VrpDetailedSolution(mockSolution, mockMatrix) shouldBe result
    }

    test("currentDetailedSolution should return null when matrix exists but solution does not") {
        val problemId = 1L
        val mockMatrix = mockk<VrpProblemMatrix>(relaxed = true)

        coEvery { vrpProblemPort.getMatrixById(problemId) } returns mockMatrix
        coEvery { vrpSolverSolutionPort.currentSolutionRequest(problemId) } returns null

        val result = solverRepository.currentDetailedSolution(problemId)

        assertNull(result)
    }

    test("currentDetailedSolution should return null when matrix does not exist") {
        val problemId = 1L

        coEvery { vrpProblemPort.getMatrixById(problemId) } returns null

        val result = solverRepository.currentDetailedSolution(problemId)

        coVerify(exactly = 0) { vrpSolverSolutionPort.currentSolutionRequest(problemId) }
        assertNull(result)
    }
})