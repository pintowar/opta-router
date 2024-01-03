package io.github.pintowar.opta.router.config.rest

import io.github.pintowar.opta.router.adapters.database.VrpLocationJooqAdapter
import io.github.pintowar.opta.router.adapters.database.VrpProblemJooqAdapter
import io.github.pintowar.opta.router.adapters.database.VrpSolverRequestJooqAdapter
import io.github.pintowar.opta.router.adapters.database.VrpSolverSolutionJooqAdapter
import io.github.pintowar.opta.router.adapters.database.VrpVehicleJooqAdapter
import io.github.pintowar.opta.router.config.ConfigData
import io.github.pintowar.opta.router.core.domain.ports.repo.VrpLocationPort
import io.github.pintowar.opta.router.core.domain.ports.repo.VrpProblemPort
import io.github.pintowar.opta.router.core.domain.ports.repo.VrpSolverRequestPort
import io.github.pintowar.opta.router.core.domain.ports.repo.VrpSolverSolutionPort
import io.github.pintowar.opta.router.core.domain.ports.repo.VrpVehiclePort
import io.github.pintowar.opta.router.core.domain.ports.service.GeoPort
import io.github.pintowar.opta.router.core.serialization.Serde
import org.jooq.DSLContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile(ConfigData.REST_PROFILE)
class RepositoryConfig {

    @Bean
    fun vrpProblemRepository(
        serde: Serde,
        geoPort: GeoPort,
        dslContext: DSLContext
    ): VrpProblemPort = VrpProblemJooqAdapter(dslContext, geoPort, serde)

    @Bean
    fun vrpLocationRepository(
        dslContext: DSLContext
    ): VrpLocationPort = VrpLocationJooqAdapter(dslContext)

    @Bean
    fun vrpVehicleRepository(
        dslContext: DSLContext
    ): VrpVehiclePort = VrpVehicleJooqAdapter(dslContext)

    @Bean
    fun vrpSolverSolutionRepository(
        serde: Serde,
        dslContext: DSLContext
    ): VrpSolverSolutionPort = VrpSolverSolutionJooqAdapter(serde, dslContext)

    @Bean
    fun vrpSolverRequestRepository(
        dslContext: DSLContext
    ): VrpSolverRequestPort = VrpSolverRequestJooqAdapter(dslContext)
}