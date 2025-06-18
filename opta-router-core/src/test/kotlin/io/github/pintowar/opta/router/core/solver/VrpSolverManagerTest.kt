package io.github.pintowar.opta.router.core.solver

import io.github.pintowar.opta.router.core.domain.messages.SolutionRequestCommand
import io.github.pintowar.opta.router.core.domain.models.SolverStatus
import io.github.pintowar.opta.router.core.domain.models.VrpDetailedSolution
import io.github.pintowar.opta.router.core.domain.models.VrpSolution
import io.github.pintowar.opta.router.core.domain.models.matrix.VrpCachedMatrix
import io.github.pintowar.opta.router.core.solver.spi.Solver
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.test.testCoroutineScheduler
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.Duration
import java.util.UUID

class VrpSolverManagerTest :
    FunSpec({

        beforeSpec {
            mockkObject(Solver)
        }

        afterSpec {
            unmockkAll()
        }

        context("VrpSolverManager") {
            val timeLimit = Duration.ofSeconds(10)
            val solverKey = UUID.randomUUID()
            val initialSolution =
                mockk<VrpSolution> {
                    every { getTotalDistance() } returns BigDecimal.ZERO
                }
            val detailedSolution =
                mockk<VrpDetailedSolution> {
                    every { solution } returns initialSolution
                    every { matrix } returns mockk()
                }
            val solverName = "testSolver"

            context("solve function") {
                test("should return flow of SolutionRequestCommand") {
                    val solverManager = VrpSolverManager(timeLimit)
                    val mockSolver = mockk<Solver>()
                    val updatedSolution =
                        mockk<VrpSolution> {
                            every { getTotalDistance() } returns BigDecimal.valueOf(100)
                        }

                    every { Solver.getSolverByName(solverName) } returns mockSolver
                    every {
                        mockSolver.solve(
                            initialSolution,
                            any<VrpCachedMatrix>(),
                            any<SolverConfig>()
                        )
                    } returns flowOf(updatedSolution)

                    val resultFlow = solverManager.solve(solverKey, detailedSolution, solverName)
                    val commands = resultFlow.take(2).toList() // Take 2: RUNNING and TERMINATED

                    commands.size shouldBe 2
                    (commands[0].solutionRequest.status shouldBe SolverStatus.RUNNING)
                    (commands[0].solutionRequest.solverKey shouldBe solverKey)
                    (commands[0].solutionRequest.solution shouldBe updatedSolution)
                    (commands[0].clear shouldBe false)

                    (commands[1].solutionRequest.status shouldBe SolverStatus.TERMINATED)
                    (commands[1].solutionRequest.solverKey shouldBe solverKey)
                    (commands[1].solutionRequest.solution shouldBe updatedSolution)
                    (commands[1].clear shouldBe false)

                    solverManager.destroy()
                }

                test("should return terminated flow if solverKey is blacklisted") {
                    val solverManager = VrpSolverManager(timeLimit)
                    // Simulate blacklisting by calling cancelSolver with ENQUEUED status
                    solverManager.cancelSolver(solverKey, SolverStatus.ENQUEUED, false)

                    val resultFlow = solverManager.solve(solverKey, detailedSolution, solverName)
                    val commands = resultFlow.toList()

                    commands.size shouldBe 1
                    (commands[0].solutionRequest.status shouldBe SolverStatus.TERMINATED)
                    (commands[0].solutionRequest.solverKey shouldBe solverKey)
                    (commands[0].solutionRequest.solution shouldBe initialSolution)
                    (commands[0].clear shouldBe false)

                    solverManager.destroy()
                }

                test("should return empty flow if solverKey is already present") {
                    val solverManager = VrpSolverManager(timeLimit)
                    val mockSolver = mockk<Solver>()

                    every { Solver.getSolverByName(solverName) } returns mockSolver
                    every {
                        mockSolver.solve(
                            initialSolution,
                            any<VrpCachedMatrix>(),
                            any<SolverConfig>()
                        )
                    } returns flowOf(initialSolution)

                    // Start a solver to make the key present
                    solverManager.solve(solverKey, detailedSolution, solverName)

                    val resultFlow = solverManager.solve(solverKey, detailedSolution, solverName)
                    val commands = resultFlow.toList()

                    commands.size shouldBe 0

                    solverManager.destroy()
                }
            }

            context("cancelSolver function") {
                test("should cancel an active solver").config(coroutineTestScope = true) {
                    val solverManager = VrpSolverManager(timeLimit)
                    val mockSolver = mockk<Solver>()
                    val updatedSolution =
                        mockk<VrpSolution> {
                            every { getTotalDistance() } returns BigDecimal.valueOf(100)
                        }

                    every { Solver.getSolverByName(solverName) } returns mockSolver
                    every {
                        mockSolver.solve(
                            initialSolution,
                            any<VrpCachedMatrix>(),
                            any<SolverConfig>()
                        )
                    } returns
                        flow {
                            delay(5000)
                            emit(updatedSolution)
                        }

                    val commands = mutableListOf<SolutionRequestCommand>()
                    launch {
                        solverManager.solve(solverKey, detailedSolution, solverName).collect(commands::add)
                    }
                    testCoroutineScheduler.advanceTimeBy(1000)

                    solverManager.cancelSolver(solverKey, SolverStatus.RUNNING, false)

                    // Should still receive RUNNING and TERMINATED due to onCompletion
                    commands.size shouldBe 1

                    (commands[0].solutionRequest.status shouldBe SolverStatus.TERMINATED)
                    (commands[0].solutionRequest.solution.getTotalDistance() shouldBe BigDecimal.ZERO)
                    (commands[0].clear shouldBe false) // clear is false

                    solverManager.destroy()
                }

                test("should clear if clear is true").config(coroutineTestScope = true) {
                    val solverManager = VrpSolverManager(timeLimit)
                    val mockSolver = mockk<Solver>()
                    val updatedSolution =
                        mockk<VrpSolution> {
                            every { getTotalDistance() } returns BigDecimal.valueOf(100)
                        }

                    every { Solver.getSolverByName(solverName) } returns mockSolver
                    every {
                        mockSolver.solve(
                            initialSolution,
                            any<VrpCachedMatrix>(),
                            any<SolverConfig>()
                        )
                    } returns
                        flow {
                            emit(updatedSolution)
                            delay(5000)
                        }

                    val commands = mutableListOf<SolutionRequestCommand>()
                    launch {
                        solverManager.solve(solverKey, detailedSolution, solverName).collect(commands::add)
                    }
                    testCoroutineScheduler.advanceTimeBy(1000)

                    solverManager.cancelSolver(solverKey, SolverStatus.RUNNING, true)

                    // Should still receive RUNNING and TERMINATED due to onCompletion
                    commands.size shouldBe 2

                    (commands[1].solutionRequest.status shouldBe SolverStatus.TERMINATED)
                    (commands[1].solutionRequest.solution.getTotalDistance() shouldBe BigDecimal.valueOf(100))
                    (commands[1].clear shouldBe true) // clear is true

                    solverManager.destroy()
                }

                test("should blacklist key if status is ENQUEUED").config(coroutineTestScope = true) {
                    val solverManager = VrpSolverManager(timeLimit)
                    solverManager.cancelSolver(solverKey, SolverStatus.ENQUEUED, false)

                    val resultFlow = solverManager.solve(solverKey, detailedSolution, solverName)
                    val commands = resultFlow.toList()

                    commands.size shouldBe 1
                    (commands[0].solutionRequest.status shouldBe SolverStatus.TERMINATED)
                    (commands[0].solutionRequest.solverKey shouldBe solverKey)
                    (commands[0].clear shouldBe false)

                    solverManager.destroy()
                }
            }

            context("destroy function") {
                test("should cancel all active jobs").config(coroutineTestScope = true) {
                    val solverManager = VrpSolverManager(timeLimit)
                    val mockSolver = mockk<Solver>()
                    val updatedSolution =
                        mockk<VrpSolution> {
                            every { getTotalDistance() } returns BigDecimal.valueOf(100)
                        }

                    every { Solver.getSolverByName(solverName) } returns mockSolver
                    every {
                        mockSolver.solve(
                            initialSolution,
                            any<VrpCachedMatrix>(),
                            any<SolverConfig>()
                        )
                    } returns flowOf(updatedSolution)

                    val commands = mutableListOf<SolutionRequestCommand>()
                    launch {
                        solverManager.solve(solverKey, detailedSolution, solverName).collect(commands::add)
                    }

                    solverManager.destroy()

                    commands.size shouldBe 0

                    // Verify that no more commands are sent after destroy
                    val newResultFlow = solverManager.solve(UUID.randomUUID(), detailedSolution, solverName)
                    val newCommands = newResultFlow.toList()
                    newCommands.size shouldBe 0 // No new solvers should start after destroy
                }
            }
        }
    })