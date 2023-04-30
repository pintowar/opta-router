import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootBuildImage

plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spotless)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.bundles.kotlin)
    implementation(libs.bundles.spring) {
        exclude(module = "spring-boot-starter-logging")
    }
    implementation(libs.springdoc.openapi)

    implementation(libs.bundles.optaplanner) {
        exclude(group = "com.google.protobuf")
        exclude(group = "com.sun.xml.bind")
    }
    implementation(libs.graphhopper.core)

    implementation(libs.bundles.jackson)
    runtimeOnly(libs.slf4j)

    testImplementation(libs.spring.test)
}

tasks {
    withType<KotlinCompile> {
        compilerOptions {
            freeCompilerArgs.set(listOf("-Xjsr305=strict"))
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    if (project.hasProperty("prod")) {
        processResources {
            val webCli = ":client"
            dependsOn("$webCli:build")

            doLast {
                val origin = project(webCli).buildDir.absolutePath
                val dest = "${project.buildDir.absolutePath}/resources/main/public"
                copy {
                    from(origin)
                    into(dest)
                }
                logger.quiet("Cli Resources: move from $origin to $dest")
            }
        }
    }

    named<BootBuildImage>("bootBuildImage") {
        val isSnapshot = "${project.version}".endsWith("-SNAPSHOT")
        val imgName = "pintowar/${rootProject.name}"
        val latestTag = if (!isSnapshot) listOf("latest") else emptyList()
        val tagNames = (listOf("${project.version}") + latestTag).map { "${imgName}:$it" }

        imageName.set(imgName)
        tags.set(tagNames)
        verboseLogging.set(true)


        buildpacks.set(
            listOf(
                "gcr.io/paketo-buildpacks/adoptium",
                "urn:cnb:builder:paketo-buildpacks/java"
            )
        )
        environment.putAll(
            mapOf(
                "BP_JVM_TYPE" to "JDK",
//                "BPE_APPEND_JAVA_TOOL_OPTIONS" to "-Duser.timezone=UTC -Djava.security.egd=file:/dev/./urandom"
//                "GRAPH_OSM_PATH" to "/opt/pbf/belgium-latest.osm.pbf",
            )
        )

        publish.set(!isSnapshot)
//        docker {
//            publishRegistry {
//                username.set()
//                password.set()
//                url.set()
//                email.set()
//            }
//        }

    }
}

spotless {
    kotlin {
        ktlint()
            .setEditorConfigPath("${rootProject.projectDir}/.editorconfig")
//    licenseHeaderFile()
    }
}