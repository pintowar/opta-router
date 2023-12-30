package io.github.pintowar.opta.router.config

import io.github.pintowar.opta.router.core.domain.ports.GeoPort
import io.github.pintowar.opta.router.core.solver.SolverPanelStorage
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.ConcurrentHashMap

@Configuration
class SessionPanelStorageConfig {

    @Bean
    fun sessionPanelStorage(geoPort: GeoPort): SolverPanelStorage =
        SolverPanelStorage(ConcurrentHashMap(), geoPort)

}

