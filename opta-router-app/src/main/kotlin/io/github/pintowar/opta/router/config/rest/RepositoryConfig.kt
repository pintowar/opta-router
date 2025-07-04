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
    /**
     * Creates a VRP problem repository.
     *
     * @param serde The serialization/deserialization utility.
     * @param geoPort The geo port.
     * @param dslContext The JOOQ DSL context.
     * @return The VRP problem repository.
     */
    @Bean
    fun vrpProblemRepository(
        serde: Serde,
        geoPort: GeoPort,
        dslContext: DSLContext
    ): VrpProblemPort = VrpProblemJooqAdapter(dslContext, geoPort, serde)

    /**
     * Creates a VRP location repository.
     *
     * @param dslContext The JOOQ DSL context.
     * @return The VRP location repository.
     */
    @Bean
    fun vrpLocationRepository(dslContext: DSLContext): VrpLocationPort = VrpLocationJooqAdapter(dslContext)

    /**
     * Creates a VRP vehicle repository.
     *
     * @param dslContext The JOOQ DSL context.
     * @return The VRP vehicle repository.
     */
    @Bean
    fun vrpVehicleRepository(dslContext: DSLContext): VrpVehiclePort = VrpVehicleJooqAdapter(dslContext)

    /**
     * Creates a VRP solver solution repository.
     *
     * @param serde The serialization/deserialization utility.
     * @param dslContext The JOOQ DSL context.
     * @return The VRP solver solution repository.
     */
    @Bean
    fun vrpSolverSolutionRepository(
        serde: Serde,
        dslContext: DSLContext
    ): VrpSolverSolutionPort = VrpSolverSolutionJooqAdapter(serde, dslContext)

    /**
     * Creates a VRP solver request repository.
     *
     * @param dslContext The JOOQ DSL context.
     * @return The VRP solver request repository.
     */
    @Bean
    fun vrpSolverRequestRepository(dslContext: DSLContext): VrpSolverRequestPort =
        VrpSolverRequestJooqAdapter(dslContext)
}