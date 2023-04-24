package com.github

import com.github.util.GraphWrapper
import com.graphhopper.reader.osm.GraphHopperOSM
import com.graphhopper.routing.util.CarFlagEncoder
import com.graphhopper.routing.util.EncodingManager
import jakarta.servlet.http.HttpServletRequest
import org.apache.commons.lang3.exception.ExceptionUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpEntity
import org.springframework.http.HttpStatus
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executor


@EnableAsync
@SpringBootApplication
class Application {

    /**
     * The creation of the Graphhopper Wrapper.
     */
    @Bean
    fun graphHopper(@Value("\${app.graph.osm.path}") path: String,
                    @Value("\${app.graph.osm.location}") location: String): GraphWrapper {
        val em = EncodingManager.start().setEnableInstructions(false).add(CarFlagEncoder()).build()

        val gh = GraphHopperOSM().forServer().setDataReaderFile(path)
                .setGraphHopperLocation(location)
                .setCHEnabled(true)
                .setMinNetworkSize(200, 200)
                .setEncodingManager(em)

        gh.chFactoryDecorator.preparationThreads = Runtime.getRuntime().availableProcessors()
        gh.chFactoryDecorator.weightingsAsStrings = listOf("fastest", "shortest")

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

    /**
     * Creates a ConcurrentHashMap that associates the Socket Session ID to the Http Session ID.
     * @see StompConnectEventListener
     * @see com.github.opta.VehicleRoutingSolverService
     */
    @Bean
    fun sessionWebSocket() = ConcurrentHashMap<String, String>()

}

@ControllerAdvice
class GlobalExceptionHandler {

    /**
     * This Exception Handler is defined to prevent the annoying ClientAbortException (IOException: Broken pipe)
     * to be logged.
     */
    @ExceptionHandler(IOException::class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    fun exceptionHandler(e: IOException, request: HttpServletRequest) =
            if (!ExceptionUtils.getRootCauseMessage(e).lowercase().contains("broken pipe"))
                HttpEntity(e.message ?: "Broken Pipe!!") else null
}

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}
