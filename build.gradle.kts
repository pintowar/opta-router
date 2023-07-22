import net.researchgate.release.ReleaseExtension

plugins {
    base
    id("idea")
    id("com.diffplug.spotless")
    id("net.saliman.properties")
    alias(libs.plugins.release)
}

allprojects {
    group = "io.github.pintowar"
    description = "Sample VRP Application using Kotlin + Optaplanner + Graphhopper + Spring Boot + Websockets"
}

repositories {
    mavenLocal()
    mavenCentral()
}

spotless {
    format("misc") {
        target("**/.gitignore", "README.md")
        indentWithSpaces()
        trimTrailingWhitespace()
        endWithNewline()
    }
}

configure<ReleaseExtension> {
    tagTemplate.set("v\$version")
    with(git) {
        requireBranch.set("master")
    }
}

tasks.afterReleaseBuild {
    dependsOn(":opta-router-app:jib")
}