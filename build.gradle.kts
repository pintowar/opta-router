import net.researchgate.release.ReleaseExtension

plugins {
    base
    id("idea")
    alias(libs.plugins.spotless)
    alias(libs.plugins.release)
    alias(libs.plugins.kotlin.jvm) apply false
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
    kotlinGradle {
        target("**/*.gradle.kts")
        ktlint()
    }
}

configure<ReleaseExtension> {
    tagTemplate.set("v\$version")
    with(git) {
        requireBranch.set("master")
    }
}

tasks.afterReleaseBuild {
    dependsOn(":server:jib")
}