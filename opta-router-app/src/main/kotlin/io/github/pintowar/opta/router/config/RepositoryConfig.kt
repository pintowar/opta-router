package io.github.pintowar.opta.router.config

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.pintowar.opta.router.adapters.database.*
import io.github.pintowar.opta.router.core.domain.ports.*
import org.jooq.DSLContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RepositoryConfig {

    @Bean
    fun vrpProblemRepository(
        dslContext: DSLContext
    ): VrpProblemPort = VrpProblemJooqAdapter(dslContext)

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