import com.gorylenko.GitPropertiesPluginExtension

plugins {
    id("opta-router.base")
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.git.properties)
    alias(libs.plugins.jib)
}

dependencies {
    implementation(project(":opta-router-core"))
    implementation(project(":opta-router-repo"))
    implementation(project(":opta-router-geo"))
    implementation(project(":opta-router-solver:jenetics"))
    implementation(project(":opta-router-solver:jsprit"))
//    implementation(project(":opta-router-solver:optaplanner"))
    implementation(project(":opta-router-solver:ortools"))
    implementation(project(":opta-router-solver:timefold"))

    implementation(libs.bundles.spring) {
        exclude(module = "jooq")
    }
    implementation(libs.springdoc.openapi)

    implementation(libs.bundles.camel)
    implementation(libs.bundles.jooq)
    implementation(libs.bundles.jackson)
    runtimeOnly(libs.slf4j)

    testImplementation(libs.spring.test)

    runtimeOnly(if (project.isDistProfile) libs.pg.db else libs.h2.db)
}

tasks {
    bootJar {
        requiresUnpack("**/ortools-*.jar") // This is required, so the native libraries can be unpacked at runtime
    }

    processResources {
        val webCli = ":opta-router-webcli"
        val isLocalProfile = project.isLocalProfile.also {
            if (!it) dependsOn("$webCli:build")
        }

        doLast {
            val resourceDest = "${project.buildDir.absolutePath}/resources/main"
            val appProps = project.properties.filterKeys { it == "environmentName" || it.startsWith("flyway") }

            copy {
                from("src/main/resources")
                include("**/*.yml")
                filter<org.apache.tools.ant.filters.ReplaceTokens>("tokens" to appProps)
                into(resourceDest)
                logger.quiet("Replacing properties resources")
            }
            if (!isLocalProfile) {
                val webCliOrigin = project(webCli).buildDir.absolutePath
                val webCliDest = "$resourceDest/public"
                copy {
                    from(webCliOrigin)
                    into(webCliDest)
                }
                logger.quiet("Cli Resources: move from $webCliOrigin to $webCliDest")
            }
        }
    }
}

configure<GitPropertiesPluginExtension> {
    dotGitDirectory.set(file("${project.rootDir}/.git"))
    dateFormat = "yyyy-MM-dd'T'HH:mmZ"
    dateFormatTimeZone = "GMT"
}

jib {
    from {
        image = "eclipse-temurin:17-jdk-jammy"
    }
    to {
        val tagVer = if (project.isSnapshotVersion) "snapshot" else "latest"
        image = "pintowar/${rootProject.name}:${project.buildEnv}-$tagVer"
        tags = setOf("${project.buildEnv}-${project.version}")
        auth {
            username = project.findProperty("docker.user")?.toString() ?: System.getenv("DOCKER_USER")
            password = project.findProperty("docker.pass")?.toString() ?: System.getenv("DOCKER_PASS")
        }
    }
    container {
        mainClass = "io.github.pintowar.opta.router.ApplicationKt"
        jvmFlags = listOf("-Duser.timezone=UTC", "-Djava.security.egd=file:/dev/./urandom")
        ports = listOf("8080")
        creationTime.set("USE_CURRENT_TIMESTAMP")
    }
}