package io.github.pintowar.opta.router

import com.graphhopper.GraphHopper
import com.graphhopper.config.CHProfile
import com.graphhopper.config.Profile
import io.github.pintowar.opta.router.util.GraphWrapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

@SpringBootApplication
class Application {

    /**
     * The creation of the Graphhopper Wrapper.
     */
    @Bean
    fun graphHopper(
        @Value("\${app.graph.osm.path}") path: String,
        @Value("\${app.graph.osm.location}") location: String
    ): GraphWrapper {
//        val em = EncodingManager.start().add(VehicleEncodedValues.car(PMap())).build()
        val profs = listOf("shortest").map {
            Profile("car_$it").apply {
                vehicle = "car"
                weighting = it
            }
        }

        val gh = GraphHopper().apply {
            osmFile = path
            graphHopperLocation = location
            profiles = profs
            chPreparationHandler.preparationThreads = Runtime.getRuntime().availableProcessors()
            chPreparationHandler.setCHProfiles(profs.map { CHProfile(it.name) })

            setMinNetworkSize(200)
        }

        return GraphWrapper(gh.importOrLoad())
    }

    /**
     * The creation of the Thread Pool Task Executor, to run the optimization process in background.
     * @see com.github.opta.VehicleRoutingSolverService
     */
    @Bean
    fun taskExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 2
        executor.maxPoolSize = 2
        executor.setQueueCapacity(500)
        executor.setThreadNamePrefix("VrpSolver-")
        executor.initialize()
        return executor
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}