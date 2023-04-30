import com.github.gradle.node.npm.task.NpmTask

plugins {
    id("com.github.node-gradle.node") version "3.1.1"
}

project.buildDir = file("dist")

node {
    version.set("16.15.1")
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

    register<NpmTask>("test") {
        dependsOn(npmInstall)
        group = "test"
        description = "unit tests"
        args.set(listOf("run", "coverage"))
    }

    register<Delete>("clean") {
        delete(project.buildDir)
        delete("${project.projectDir}/coverage")
    }
}