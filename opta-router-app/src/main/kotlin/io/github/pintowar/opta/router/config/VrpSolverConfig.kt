package io.github.pintowar.opta.router.config

import io.github.pintowar.opta.router.core.domain.ports.BroadcastService
import io.github.pintowar.opta.router.core.domain.ports.VrpSolverSolutionRepository
import io.github.pintowar.opta.router.core.domain.ports.VrpSolverService
import io.github.pintowar.opta.router.core.solver.OptaSolverService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
class VrpSolverConfig {

    @Bean(destroyMethod = "destroy")
    fun vrpSolverService(
        @Value("\${solver.termination.time-limit}") timeLimit: Duration,
        vrpSolverSolutionRepository: VrpSolverSolutionRepository,
        broadcastService: BroadcastService
    ): VrpSolverService {
        return OptaSolverService(timeLimit, vrpSolverSolutionRepository, broadcastService)
    }
}