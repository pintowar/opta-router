package io.github.pintowar.opta.router.config.solver

import io.github.pintowar.opta.router.config.ConfigData
import io.github.pintowar.opta.router.core.solver.VrpSolverManager
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.time.Duration

@Configuration
@Profile(ConfigData.SOLVER_PROFILE)
class VrpSolverManagerConfig {
    @Bean(destroyMethod = "destroy")
    fun vrpSolverManager(
        @Value("\${solver.termination.time-limit}") timeLimit: Duration
    ): VrpSolverManager {
        return VrpSolverManager(timeLimit)
    }
}