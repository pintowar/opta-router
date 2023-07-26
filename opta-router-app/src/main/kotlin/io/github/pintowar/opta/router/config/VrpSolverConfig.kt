package io.github.pintowar.opta.router.config

import com.hazelcast.core.HazelcastInstance
import io.github.pintowar.opta.router.adapters.handler.SolverQueueAdapter
import io.github.pintowar.opta.router.core.domain.ports.BroadcastPort
import io.github.pintowar.opta.router.core.domain.ports.SolverQueuePort
import io.github.pintowar.opta.router.core.domain.ports.VrpProblemPort
import io.github.pintowar.opta.router.core.domain.ports.VrpSolverRequestPort
import io.github.pintowar.opta.router.core.domain.ports.VrpSolverSolutionPort
import io.github.pintowar.opta.router.core.domain.repository.SolverRepository
import io.github.pintowar.opta.router.core.solver.VrpSolverManager
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

    @Bean
    fun solverQueue(hz: HazelcastInstance): SolverQueuePort = SolverQueueAdapter(hz)

    @Bean(destroyMethod = "destroy")
    fun vrpSolverManager(
        @Value("\${solver.termination.time-limit}") timeLimit: Duration,
        solverRepository: SolverRepository,
        solverQueuePort: SolverQueuePort,
        broadcastPort: BroadcastPort
    ): VrpSolverManager {
        return VrpSolverManager(timeLimit, solverQueuePort, solverRepository, broadcastPort)
    }
}