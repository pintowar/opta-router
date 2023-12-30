package io.github.pintowar.opta.router.config

import com.hazelcast.core.HazelcastInstance
import net.javacrumbs.shedlock.provider.hazelcast4.HazelcastLockProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ShedlockConfig {

    @Bean
    fun lockProvider(hazelcastInstance: HazelcastInstance): HazelcastLockProvider {
        return HazelcastLockProvider(hazelcastInstance)
    }
}