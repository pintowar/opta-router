import com.github.gradle.node.npm.task.NpmTask

plugins {
    alias(libs.plugins.node)
}

project.layout.buildDirectory.set(file("dist"))

node {
    version.set("22.16.0")
    download.set(true)
}

tasks {
    register<NpmTask>("bootRun") {
        dependsOn(npmInstall)
        group = "application"
        description = "Run the client app"
        args.set(listOf("run", "dev"))
    }

    register<NpmTask>("build") {
        dependsOn(npmInstall)
        group = "build"
        description = "Build the client bundle"
        args.set(listOf("run", "build"))
    }

    register<Task>("test") {
        logger.quiet("sorry, no tests at all :(")
    }

    register<Delete>("clean") {
        delete(project.layout.buildDirectory.get())
    }
}