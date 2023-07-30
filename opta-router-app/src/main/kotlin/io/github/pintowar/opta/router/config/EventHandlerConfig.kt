package io.github.pintowar.opta.router.config

import com.hazelcast.core.HazelcastInstance
import io.github.pintowar.opta.router.config.hz.HazelcastEventsRegistry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class EventHandlerConfig {

    @Bean(destroyMethod = "destroy")
    fun eventRegistry(hz: HazelcastInstance): HazelcastEventsRegistry {
        return HazelcastEventsRegistry(hz)
    }
}