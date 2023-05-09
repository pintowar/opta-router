package io.github.pintowar.opta.router.config

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.pintowar.opta.router.adapters.database.VrpProblemJooqRepository
import io.github.pintowar.opta.router.adapters.database.VrpSolverSolutionJooqRepository
import io.github.pintowar.opta.router.core.domain.ports.VrpProblemRepository
import io.github.pintowar.opta.router.core.domain.ports.VrpSolverSolutionRepository
import org.jooq.DSLContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RepositoryConfig {

    @Bean
    fun vrpProblemRepository(
        dslContext: DSLContext,
    ): VrpProblemRepository = VrpProblemJooqRepository(dslContext)


    @Bean
    fun vrpSolverSolutionRepository(
        objectMapper: ObjectMapper,
        dslContext: DSLContext,
        solutionRepo: VrpProblemRepository
    ): VrpSolverSolutionRepository = VrpSolverSolutionJooqRepository(objectMapper, dslContext, solutionRepo)

}