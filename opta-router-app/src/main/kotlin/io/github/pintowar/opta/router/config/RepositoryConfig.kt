package io.github.pintowar.opta.router.config

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.pintowar.opta.router.adapters.database.VrpLocationJooqAdapter
import io.github.pintowar.opta.router.adapters.database.VrpProblemJooqAdapter
import io.github.pintowar.opta.router.adapters.database.VrpSolverRequestJooqAdapter
import io.github.pintowar.opta.router.adapters.database.VrpSolverSolutionJooqAdapter
import io.github.pintowar.opta.router.adapters.database.VrpVehicleJooqAdapter
import io.github.pintowar.opta.router.core.domain.ports.GeoPort
import io.github.pintowar.opta.router.core.domain.ports.VrpLocationPort
import io.github.pintowar.opta.router.core.domain.ports.VrpProblemPort
import io.github.pintowar.opta.router.core.domain.ports.VrpSolverRequestPort
import io.github.pintowar.opta.router.core.domain.ports.VrpSolverSolutionPort
import io.github.pintowar.opta.router.core.domain.ports.VrpVehiclePort
import org.jooq.DSLContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RepositoryConfig {

    @Bean
    fun vrpProblemRepository(
        objectMapper: ObjectMapper,
        geoPort: GeoPort,
        dslContext: DSLContext
    ): VrpProblemPort = VrpProblemJooqAdapter(dslContext, geoPort, objectMapper)

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
        objectMapper: ObjectMapper,
        dslContext: DSLContext
    ): VrpSolverSolutionPort = VrpSolverSolutionJooqAdapter(objectMapper, dslContext)

    @Bean
    fun vrpSolverRequestRepository(
        dslContext: DSLContext
    ): VrpSolverRequestPort = VrpSolverRequestJooqAdapter(dslContext)
}