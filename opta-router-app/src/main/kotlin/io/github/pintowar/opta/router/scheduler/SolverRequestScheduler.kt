package io.github.pintowar.opta.router.scheduler

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.pintowar.opta.router.config.ConfigData
import io.github.pintowar.opta.router.core.domain.ports.repo.VrpSolverRequestPort
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.concurrent.TimeUnit

private val logger = KotlinLogging.logger {}

@Component
@Profile(ConfigData.REST_PROFILE)
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "24h")
class SolverRequestScheduler(
    @param:Value($$"${solver.termination.time-limit}") private val terminationTimeLimit: Duration,
    @param:Value($$"${solver.unqueue.time-limit}") private val unqueueTimeLimit: Duration,
    private val vrpSolverRequestPort: VrpSolverRequestPort
) {
    /**
     * Scheduled task to refresh abandoned solver requests. This function runs every 5 minutes
     * and updates the status of solver requests that have exceeded their time limit.
     */
    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.MINUTES)
    @SchedulerLock(name = "refreshAbandonedSolverRequests", lockAtLeastFor = "5m")
    fun refreshAbandonedSolverRequests() {
        CoroutineScope(Dispatchers.IO).launch {
            val numUpdates = vrpSolverRequestPort.refreshRunningSolverRequests(terminationTimeLimit)
            logger.info { "Refreshed $numUpdates abandoned SolverRequests" }
        }
    }

    @Scheduled(fixedDelay = 10, timeUnit = TimeUnit.SECONDS)
    @SchedulerLock(name = "refreshUnqueuedSolverRequests", lockAtLeastFor = "10s")
    fun refreshUnqueuedSolverRequests() {
        CoroutineScope(Dispatchers.IO).launch {
            val numUpdates = vrpSolverRequestPort.refreshCreatedSolverRequests(unqueueTimeLimit)
            logger.info { "Refreshed $numUpdates unqueued SolverRequests" }
        }
    }
}