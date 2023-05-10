package io.github.pintowar.opta.router.config

import io.github.pintowar.opta.router.core.domain.ports.BroadcastPort
import io.github.pintowar.opta.router.core.domain.ports.VrpProblemPort
import io.github.pintowar.opta.router.core.domain.ports.VrpSolverSolutionPort
import io.github.pintowar.opta.router.core.domain.repository.SolverRepository
import io.github.pintowar.opta.router.core.solver.OptaSolverService
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
        vrpSolverSolutionPort: VrpSolverSolutionPort
    ) = SolverRepository(vrpProblemPort, vrpSolverSolutionPort)

    @Bean // (destroyMethod = "destroy")
    fun vrpSolverService(
        @Value("\${solver.termination.time-limit}") timeLimit: Duration,
        solverRepository: SolverRepository,
        broadcastPort: BroadcastPort
    ): VrpSolverService {
        return OptaSolverService(timeLimit, solverRepository, broadcastPort)
    }
}