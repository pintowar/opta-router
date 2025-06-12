package io.github.pintowar.opta.router.config.rest

import com.hazelcast.core.HazelcastInstance
import io.github.pintowar.opta.router.config.ConfigData
import net.javacrumbs.shedlock.provider.hazelcast4.HazelcastLockProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile(ConfigData.REST_PROFILE)
class ShedlockConfig {
    @Bean
    fun lockProvider(hazelcastInstance: HazelcastInstance): HazelcastLockProvider {
        return HazelcastLockProvider(hazelcastInstance)
    }
}