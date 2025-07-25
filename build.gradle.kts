import net.researchgate.release.ReleaseExtension
import java.time.LocalDate

plugins {
    base
    id("idea")
    id("jacoco-report-aggregation")
    id("com.diffplug.spotless")
    id("net.saliman.properties")
    id("org.jetbrains.dokka")
    alias(libs.plugins.release)
    alias(libs.plugins.versions)
    alias(libs.plugins.sonarqube)
    alias(libs.plugins.jreleaser)
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
            enabled.set(!isSnapshotVersion)

            changelog {
                enabled.set(false)
            }
            branchPush.set("master")
            releaseName.set("v$version")
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

sonarqube {
    properties {
        val sonarToken = project.findProperty("sonar.token")?.toString() ?: System.getenv("SONAR_TOKEN")
        val jacocoReportPath = project.layout.buildDirectory.dir("reports/jacoco/testCodeCoverageReport").get().asFile.absolutePath
        val lcovReportPath = project.layout.buildDirectory.dir("reports/coverage").get().asFile.absolutePath

        property("sonar.sourceEncoding", "UTF-8")
        property("sonar.organization", "pintowar")
        property("sonar.projectName", "opta-router")
        property("sonar.projectKey", "pintowar_opta-router")
        property("sonar.projectVersion", project.version.toString())
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.token", sonarToken)
        property("sonar.verbose", true)
        property("sonar.github.repository", "pintowar/opta-router")
        property("sonar.coverage.jacoco.xmlReportPaths", "$jacocoReportPath/testCodeCoverageReport.xml")
        property("sonar.javascript.lcov.reportPaths", "$lcovReportPath/lcov.info")
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

tasks.register("fullTestCoverageReport") {
    val webCli = ":opta-router-webcli"
    dependsOn("testCodeCoverageReport", "${webCli}:coverage")
    group = "verification"
    description = "Full Test Coverage Report"
    doLast {
        copy {
            from(project(webCli).layout.projectDirectory.dir("coverage"))
            into(layout.buildDirectory.dir("reports/coverage"))
        }
    }
}

tasks.sonar {
    dependsOn(":fullTestCoverageReport")
}

tasks.jreleaserRelease {
    dependsOn(":assembleApp")
    dependsOn(":opta-router-app:jib")
}