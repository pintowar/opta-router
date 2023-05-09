package io.github.pintowar.opta.router.config

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.pintowar.opta.router.adapters.database.jooq.SolutionJooqRepository
import io.github.pintowar.opta.router.adapters.database.jooq.SolverJooqRepository
import io.github.pintowar.opta.router.core.domain.ports.SolutionRepository
import io.github.pintowar.opta.router.core.domain.ports.SolverRepository
import org.jooq.DSLContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RepositoryConfig {

    @Bean
    fun solutionRepository(
        dslContext: DSLContext,
    ): SolutionRepository = SolutionJooqRepository(dslContext)


    @Bean
    fun solverRepository(
        objectMapper: ObjectMapper,
        dslContext: DSLContext,
        solutionRepo: SolutionRepository
    ): SolverRepository = SolverJooqRepository(objectMapper, dslContext, solutionRepo)

}