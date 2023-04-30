plugins {
    base
    id("idea")
    alias(libs.plugins.spotless)
}

allprojects {
    group = "io.github.pintowar"
    description = "Sample VRP Application using Kotlin + Optaplanner + Graphhopper + Spring Boot + Websockets"
}

repositories {
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