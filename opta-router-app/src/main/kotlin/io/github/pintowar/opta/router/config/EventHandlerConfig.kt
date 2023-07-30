package io.github.pintowar.opta.router.config

import com.hazelcast.core.HazelcastInstance
import io.github.pintowar.opta.router.adapters.handler.HazelcastEventsHandler
import io.github.pintowar.opta.router.core.domain.ports.BroadcastPort
import io.github.pintowar.opta.router.core.domain.repository.SolverRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class EventHandlerConfig {

    @Bean(destroyMethod = "destroy")
    fun eventHandler(
        solverRepository: SolverRepository,
        broadcastPort: BroadcastPort,
        hz: HazelcastInstance
    ): HazelcastEventsHandler {
        return HazelcastEventsHandler(hz)
    }
}