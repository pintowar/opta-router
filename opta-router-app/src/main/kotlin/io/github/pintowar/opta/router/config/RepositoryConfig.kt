package io.github.pintowar.opta.router.config

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.pintowar.opta.router.adapters.database.VrpProblemJooqAdapter
import io.github.pintowar.opta.router.adapters.database.VrpSolverSolutionJooqAdapter
import io.github.pintowar.opta.router.core.domain.ports.VrpProblemPort
import io.github.pintowar.opta.router.core.domain.ports.VrpSolverSolutionPort
import org.jooq.DSLContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RepositoryConfig {

    @Bean
    fun vrpProblemRepository(
        dslContext: DSLContext,
    ): VrpProblemPort = VrpProblemJooqAdapter(dslContext)


    @Bean
    fun vrpSolverSolutionRepository(
        objectMapper: ObjectMapper,
        dslContext: DSLContext,
    ): VrpSolverSolutionPort = VrpSolverSolutionJooqAdapter(objectMapper, dslContext)

}