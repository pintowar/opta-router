package io.github.pintowar.opta.router

import com.graphhopper.GraphHopper
import com.graphhopper.config.CHProfile
import com.graphhopper.config.Profile
import io.github.pintowar.opta.router.adapters.geo.GraphHopperGeoService
import io.github.pintowar.opta.router.core.domain.models.SolverPanel
import io.github.pintowar.opta.router.core.domain.ports.GeoService
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import java.util.concurrent.ConcurrentHashMap

@SpringBootApplication
class Application {

    /**
     * The creation of the Graphhopper Wrapper.
     */
    @Bean
    fun graphHopper(
        @Value("\${app.graph.osm.path}") path: String,
        @Value("\${app.graph.osm.location}") location: String
    ): GeoService {
        val profs = listOf(
            Profile(GraphHopperGeoService.PROFILE).apply {
                vehicle = GraphHopperGeoService.VEHICLE
                weighting = GraphHopperGeoService.WEIGHTING
            }
        )

        val gh = GraphHopper().apply {
            osmFile = path
            graphHopperLocation = location
            profiles = profs
            chPreparationHandler.preparationThreads = Runtime.getRuntime().availableProcessors()
            chPreparationHandler.setCHProfiles(profs.map { CHProfile(it.name) })

            setMinNetworkSize(200)
        }

        return GraphHopperGeoService(gh.importOrLoad())
    }

    @Bean
    fun sessionPanel(): MutableMap<String, SolverPanel> = ConcurrentHashMap()
}

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}