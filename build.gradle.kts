import net.researchgate.release.ReleaseExtension
import java.time.LocalDate

plugins {
    base
    id("idea")
    id("jacoco-report-aggregation")
    id("com.diffplug.spotless")
    id("net.saliman.properties")
    id("org.jreleaser") version "1.19.0"
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

jreleaser {
    project {
        authors.set(listOf("Thiago Oliveira Pinheiro"))
        license.set("Apache-2.0")
        copyright.set("Copyright (C) ${LocalDate.now().year} Thiago Oliveira Pinheiro")
        links {
            homepage.set("https://github.com/pintowar/opta-router")
        }
    }
    release {
        github {
            enabled.set(true)
            repoOwner.set("pintowar")
            name.set("opta-router")
            host.set("github.com")

            releaseName.set("v$version")
            tagName.set("v$version")
            draft.set(isSnapshotVersion)
            prerelease.enabled.set(isSnapshotVersion)
            skipTag.set(isSnapshotVersion)
            overwrite.set(isSnapshotVersion)
            update { enabled.set(isSnapshotVersion) }
        }
    }
    distributions {
        create("opta-router") {
            distributionType.set(org.jreleaser.model.Distribution.DistributionType.SINGLE_JAR)
            artifact {
                path.set(file("$rootDir/build/app-${version}.jar"))
            }
        }
    }
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
            rename { "app-${version}.jar" }
        }
    }
}

tasks.jreleaserRelease {
    dependsOn(":assembleApp")
    dependsOn(":opta-router-app:jib")
}