package io.github.pintowar.opta.router

import io.github.pintowar.opta.router.core.domain.models.SolverPanel
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.EnableScheduling
import java.util.concurrent.ConcurrentHashMap

@EnableScheduling
@SpringBootApplication
class Application {

    @Bean
    fun sessionPanel(): MutableMap<String, SolverPanel> = ConcurrentHashMap()
}

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}