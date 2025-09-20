package io.github.pintowar.opta.router.scheduler

import io.github.pintowar.opta.router.core.domain.ports.repo.VrpSolverRequestPort
import io.kotest.core.spec.style.FunSpec
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.time.Duration

class SolverRequestSchedulerTest :
    FunSpec({

        val vrpSolverRequestPort = mockk<VrpSolverRequestPort>()
        val scheduler = SolverRequestScheduler(Duration.ofMinutes(5), Duration.ofSeconds(10), vrpSolverRequestPort)

        test("refreshAbandonedSolverRequests should call the repository to refresh running requests") {
            coEvery { vrpSolverRequestPort.refreshRunningSolverRequests(any()) } returns 2

            scheduler.refreshAbandonedSolverRequests()

            coVerify { vrpSolverRequestPort.refreshRunningSolverRequests(Duration.ofMinutes(5)) }
        }

        test("refreshUnqueuedSolverRequests should call the repository to refresh created requests") {
            coEvery { vrpSolverRequestPort.refreshCreatedSolverRequests(any()) } returns 3

            scheduler.refreshUnqueuedSolverRequests()

            coVerify { vrpSolverRequestPort.refreshCreatedSolverRequests(Duration.ofSeconds(10)) }
        }
    })