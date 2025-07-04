package io.github.pintowar.opta.router.config.rest

import io.github.pintowar.opta.router.config.ConfigData
import io.github.pintowar.opta.router.core.domain.ports.service.GeoPort
import io.github.pintowar.opta.router.core.solver.SolverPanelStorage
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.util.concurrent.ConcurrentHashMap

@Configuration
@Profile(ConfigData.REST_PROFILE)
class SessionPanelStorageConfig {
    /**
     * Creates a solver panel storage.
     *
     * @param geoPort The geo port.
     * @return The solver panel storage.
     */
    @Bean
    fun sessionPanelStorage(geoPort: GeoPort): SolverPanelStorage = SolverPanelStorage(ConcurrentHashMap(), geoPort)
}