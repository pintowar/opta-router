package io.github.pintowar.opta.router.config

import io.github.pintowar.opta.router.core.domain.ports.BroadcastPort
import io.github.pintowar.opta.router.core.domain.ports.SolverEventsPort
import io.github.pintowar.opta.router.core.domain.ports.VrpProblemPort
import io.github.pintowar.opta.router.core.domain.ports.VrpSolverRequestPort
import io.github.pintowar.opta.router.core.domain.ports.VrpSolverSolutionPort
import io.github.pintowar.opta.router.core.domain.repository.SolverRepository
import io.github.pintowar.opta.router.core.solver.VrpSolverManager
import io.github.pintowar.opta.router.core.solver.VrpSolverService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
class VrpSolverConfig {

    @Bean
    fun solverRepository(
        vrpProblemPort: VrpProblemPort,
        vrpSolverSolutionPort: VrpSolverSolutionPort,
        vrpSolverRequestPort: VrpSolverRequestPort
    ) = SolverRepository(vrpProblemPort, vrpSolverSolutionPort, vrpSolverRequestPort)

    @Bean(destroyMethod = "destroy")
    fun vrpSolverManager(
        @Value("\${solver.termination.time-limit}") timeLimit: Duration
    ): VrpSolverManager {
        return VrpSolverManager(timeLimit)
    }

    @Bean
    fun vrpSolverService(
        solverRepository: SolverRepository,
        solverEventsPort: SolverEventsPort,
        broadcastPort: BroadcastPort
    ): VrpSolverService {
        return VrpSolverService(solverEventsPort, solverRepository, broadcastPort)
    }
}