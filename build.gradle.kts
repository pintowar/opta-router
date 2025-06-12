import net.researchgate.release.ReleaseExtension

plugins {
    base
    id("idea")
    id("jacoco-report-aggregation")
    id("com.diffplug.spotless")
    id("net.saliman.properties")
    alias(libs.plugins.release)
}

allprojects {
    group = "io.github.pintowar"
    description = "Sample CVRP Application using Kotlin + Jenetics/Jsprit/Optaplanner/Or-Tools/Timefold + Graphhopper + Spring Boot + Websockets"
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    rootProject.subprojects.forEach(::jacocoAggregation)
}

reporting {
    reports {
        val testCodeCoverageReport by creating(JacocoCoverageReport::class) {
            testSuiteName.set("test")
        }
    }
}

tasks.check {
    dependsOn(tasks.named<JacocoReport>("testCodeCoverageReport"))
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

tasks.register("assembleApp") {
    val webServ = ":opta-router-app"
    dependsOn("${webServ}:build")
    group = "build"
    description = "Build web app"
    doLast {
        copy {
            from(files(project(webServ).layout.buildDirectory.dir("libs").get())) {
                include("opta-router-app-${version}.jar")
            }
            into("$rootDir/build/app.jar")
        }
    }
}