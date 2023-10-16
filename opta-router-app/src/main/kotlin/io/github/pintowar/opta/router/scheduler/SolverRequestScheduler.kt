package io.github.pintowar.opta.router.scheduler

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.pintowar.opta.router.core.domain.ports.VrpSolverRequestPort
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.concurrent.TimeUnit

private val logger = KotlinLogging.logger {}

@Component
@Profile("scheduler")
@EnableScheduling
class SolverRequestScheduler(
    @Value("\${solver.termination.time-limit}") private val timeLimit: Duration,
    private val vrpSolverRequestPort: VrpSolverRequestPort
) {

    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.MINUTES)
    fun refreshAbandonedSolverRequests() {
        val numUpdates = vrpSolverRequestPort.refreshSolverRequests(timeLimit)
        logger.info { "Refreshed $numUpdates abandoned SolverRequests" }
    }
}