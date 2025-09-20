import net.researchgate.release.ReleaseExtension
import org.jreleaser.model.Active
import java.time.LocalDate

plugins {
    base
    id("idea")
    id("jacoco-report-aggregation")
    id("com.diffplug.spotless")
    id("net.saliman.properties")
    alias(libs.plugins.release)
    alias(libs.plugins.versions)
    alias(libs.plugins.sonarqube)
    alias(libs.plugins.jreleaser)
    alias(libs.plugins.dokka)
}

allprojects {
    group = "io.github.pintowar"
    description =
        "Sample CVRP Application using Kotlin + Jenetics/Jsprit/Optaplanner/Or-Tools/Timefold + Graphhopper + Spring Boot + Websockets"
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
    assemble {
        javaArchive {
            create("app") {
                active.set(Active.ALWAYS)
                java {
                    mainClass.set("io.github.pintowar.opta.router.ApplicationKt")
                    jvmOptions {
                        universal("-Duser.timezone=UTC -Djava.security.egd=file:/dev/./urandom")
                    }
                }
            }
        }
    }
    release {
        github {
            enabled.set(!isSnapshotVersion)

            changelog {
                enabled.set(false)
            }
            branch.set("master")
            releaseName.set("v$version")
        }
    }
    packagers {
        docker {
            active.assign(Active.ALWAYS)
            baseImage.set("eclipse-temurin:21-jre-noble")

            val tagVer = if (isSnapshotVersion) "snapshot" else "latest"
            imageName("${rootProject.name}:${buildEnv}-$tagVer")
            imageName("${rootProject.name}:${buildEnv}-$version")

            registries {
                create("docker.io") {
                    externalLogin.set(true)

                    username.set(findProperty("docker.user")?.toString() ?: System.getenv("DOCKER_USER"))
                    password.set(findProperty("docker.pass")?.toString() ?: System.getenv("DOCKER_PASS"))
                }
            }
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
}

tasks.jreleaserPackage {
    dependsOn(":assembleApp")
}