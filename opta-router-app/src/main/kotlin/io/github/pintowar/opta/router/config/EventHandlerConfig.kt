package io.github.pintowar.opta.router.config

import io.github.pintowar.opta.router.adapters.handler.SpringEventsHandler
import io.github.pintowar.opta.router.core.domain.ports.BroadcastPort
import io.github.pintowar.opta.router.core.domain.repository.SolverRepository
import io.github.pintowar.opta.router.core.solver.VrpSolverService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.ApplicationEventMulticaster
import org.springframework.context.event.SimpleApplicationEventMulticaster
import org.springframework.core.task.SimpleAsyncTaskExecutor


@Configuration
class EventHandlerConfig {

    @Bean(name = ["applicationEventMulticaster"])
    fun simpleApplicationEventMulticaster(): ApplicationEventMulticaster {
        return SimpleApplicationEventMulticaster().apply {
            this.setTaskExecutor(SimpleAsyncTaskExecutor().apply {
                concurrencyLimit = 2
            })
        }
    }

    @Bean
    fun eventHandler(
        solver: VrpSolverService,
        solverRepository: SolverRepository,
        broadcastPort: BroadcastPort
    ): SpringEventsHandler {
        return SpringEventsHandler(solver, solverRepository, broadcastPort)
    }
}