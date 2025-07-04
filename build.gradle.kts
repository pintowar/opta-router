import net.researchgate.release.ReleaseExtension

plugins {
    base
    id("idea")
    id("jacoco-report-aggregation")
    id("com.diffplug.spotless")
    id("net.saliman.properties")
    id("org.jetbrains.dokka")
    alias(libs.plugins.release)
    alias(libs.plugins.versions)
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
    allJacocoSubModules.forEach(::jacocoAggregation)
    allJacocoSubModules.forEach(::dokka)
}

reporting {
    reports {
        val testCodeCoverageReport by creating(JacocoCoverageReport::class) {
            testSuiteName.set("test")
            reportTask.get().classDirectories.setFrom(reportTask.get().classDirectories.map {
                fileTree(it).matching {
                    exclude(excludedJacocoPackages)
                }
            })
        }
    }
}

tasks.check {
    dependsOn(tasks.named<JacocoReport>("testCodeCoverageReport"))
}

spotless {
    format("misc") {
        target("**/.gitignore", "README.md")
        leadingTabsToSpaces()
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
            into("$rootDir/build/")
            rename { "app.jar" }
        }
    }
}