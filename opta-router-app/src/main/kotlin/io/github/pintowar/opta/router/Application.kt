package io.github.pintowar.opta.router

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class Application

/**
 * The main function of the application.
 *
 * @param args The command line arguments.
 */
fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}