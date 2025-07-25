package io.github.pintowar.opta.router.config.rest

import io.github.pintowar.opta.router.config.ConfigData
import io.github.pintowar.opta.router.core.domain.ports.events.BroadcastPort
import io.github.pintowar.opta.router.core.domain.ports.events.SolverEventsPort
import io.github.pintowar.opta.router.core.domain.ports.repo.VrpProblemPort
import io.github.pintowar.opta.router.core.domain.ports.repo.VrpSolverRequestPort
import io.github.pintowar.opta.router.core.domain.ports.repo.VrpSolverSolutionPort
import io.github.pintowar.opta.router.core.domain.repository.SolverRepository
import io.github.pintowar.opta.router.core.solver.VrpSolverService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile(ConfigData.REST_PROFILE)
class VrpSolverServiceConfig {
    /**
     * Creates a solver repository.
     *
     * @param vrpProblemPort The VRP problem port.
     * @param vrpSolverSolutionPort The VRP solver solution port.
     * @param vrpSolverRequestPort The VRP solver request port.
     * @return The solver repository.
     */
    @Bean
    fun solverRepository(
        vrpProblemPort: VrpProblemPort,
        vrpSolverSolutionPort: VrpSolverSolutionPort,
        vrpSolverRequestPort: VrpSolverRequestPort
    ) = SolverRepository(vrpProblemPort, vrpSolverSolutionPort, vrpSolverRequestPort)

    /**
     * Creates a VRP solver service.
     *
     * @param solverRepository The solver repository.
     * @param solverEventsPort The solver events port.
     * @param broadcastPort The broadcast port.
     * @return The VRP solver service.
     */
    @Bean
    fun vrpSolverService(
        solverRepository: SolverRepository,
        solverEventsPort: SolverEventsPort,
        broadcastPort: BroadcastPort
    ): VrpSolverService = VrpSolverService(broadcastPort, solverEventsPort, solverRepository)
}