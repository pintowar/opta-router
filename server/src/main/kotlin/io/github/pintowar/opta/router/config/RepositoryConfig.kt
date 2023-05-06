package io.github.pintowar.opta.router.config

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.pintowar.opta.router.adapters.database.dummy.SolutionDummyRepository
import io.github.pintowar.opta.router.adapters.database.jooq.SolutionJooqRepository
import io.github.pintowar.opta.router.adapters.database.dummy.SolverDummyRepository
import io.github.pintowar.opta.router.adapters.database.jooq.SolverJooqRepository
import io.github.pintowar.opta.router.core.domain.ports.GeoService
import io.github.pintowar.opta.router.core.domain.ports.SolutionRepository
import io.github.pintowar.opta.router.core.domain.ports.SolverRepository
import org.jooq.DSLContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RepositoryConfig(@Value("\${app.repo.dummy:true}") val dummy: Boolean) {

    @Bean
    fun solutionRepository(
        objectMapper: ObjectMapper,
        dslContext: DSLContext,
        geoService: GeoService
    ): SolutionRepository = if (dummy) {
        SolutionDummyRepository(geoService, objectMapper)
    } else {
        SolutionJooqRepository(objectMapper, dslContext)
    }

    @Bean
    fun solverRepository(
        objectMapper: ObjectMapper,
        dslContext: DSLContext,
        solutionRepo: SolutionRepository
    ): SolverRepository = if (dummy) {
        SolverDummyRepository(solutionRepo)
    } else {
        SolverJooqRepository(objectMapper, dslContext, solutionRepo)
    }

}