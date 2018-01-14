package com.github

import com.github.util.GraphWrapper
import com.graphhopper.reader.osm.GraphHopperOSM
import com.graphhopper.routing.util.EncodingManager
import org.apache.commons.lang3.exception.ExceptionUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpEntity
import org.springframework.http.HttpStatus
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2
import java.io.IOException
import java.util.concurrent.Executor
import javax.servlet.http.HttpServletRequest


@EnableSwagger2
@EnableAsync
@SpringBootApplication
class Application {

    @Bean
    fun api() = Docket(DocumentationType.SWAGGER_2)
            .select()
            .apis(RequestHandlerSelectors.any())
            .paths(PathSelectors.ant("/solve"))
            .build()

    @Bean
    fun graphHopper(@Value("\${app.graph.osm.path}") path: String,
                    @Value("\${app.graph.osm.location}") location: String) =
            GraphWrapper(GraphHopperOSM().forServer().setDataReaderFile(path)
                    .setGraphHopperLocation(location)
                    .setEncodingManager(EncodingManager("car"))
                    .setEnableInstructions(false)
                    .importOrLoad())

    @Bean
    fun asyncExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 2
        executor.maxPoolSize = 2
        executor.setQueueCapacity(500)
        executor.threadNamePrefix = "VrpSolver-"
        executor.initialize()
        return executor
    }

    @ExceptionHandler(IOException::class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    fun webSocketExceptionHandler(e: IOException, request: HttpServletRequest) =
            if (ExceptionUtils.getRootCauseMessage(e).toLowerCase().contains("broken pipe")) null
            else HttpEntity<String>(e.message)

}

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}
