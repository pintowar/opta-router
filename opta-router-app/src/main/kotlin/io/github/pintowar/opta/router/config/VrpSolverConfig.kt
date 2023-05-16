package io.github.pintowar.opta.router.config

import io.github.pintowar.opta.router.adapters.handler.SolverQueueAdapter
import io.github.pintowar.opta.router.core.domain.ports.*
import io.github.pintowar.opta.router.core.domain.repository.SolverRepository
import io.github.pintowar.opta.router.core.solver.VrpSolverService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration
import java.util.concurrent.Executors

@Configuration
class VrpSolverConfig {

    @Bean
    fun solverRepository(
        vrpProblemPort: VrpProblemPort,
        vrpSolverSolutionPort: VrpSolverSolutionPort,
        vrpSolverRequestPort: VrpSolverRequestPort
    ) = SolverRepository(vrpProblemPort, vrpSolverSolutionPort, vrpSolverRequestPort)

    @Bean
    fun solverQueue(publisher: ApplicationEventPublisher): SolverQueuePort = SolverQueueAdapter(publisher)

    @Bean // (destroyMethod = "destroy")
    fun vrpSolverService(
        @Value("\${solver.termination.time-limit}") timeLimit: Duration,
        solverRepository: SolverRepository,
        solverQueuePort: SolverQueuePort,
        broadcastPort: BroadcastPort
    ): VrpSolverService {
        return VrpSolverService(timeLimit, solverQueuePort, solverRepository, broadcastPort)
    }
}