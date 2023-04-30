import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.gorylenko.GitPropertiesPluginExtension

plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.git.properties)
    alias(libs.plugins.spotless)
    alias(libs.plugins.jib)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenLocal()
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
}

spotless {
    kotlin {
        ktlint()
            .setEditorConfigPath("${rootProject.projectDir}/.editorconfig")
//    licenseHeaderFile()
    }
}

configure<GitPropertiesPluginExtension> {
    dotGitDirectory.set(file("${project.rootDir}/.git"))
    dateFormat = "yyyy-MM-dd'T'HH:mmZ"
    dateFormatTimeZone = "GMT"
}

jib {
    val isSnapshot = "${project.version}".endsWith("-SNAPSHOT")
    from {
        image = "eclipse-temurin:17-jdk-alpine"
    }
    to {
        image = "pintowar/${rootProject.name}"
        tags = setOf("${project.version}") + (if (isSnapshot) setOf("snapshot") else setOf("latest"))
        auth {
            username = project.findProperty("docker.user")?.toString() ?: System.getenv("DOCKER_USER")
            password = project.findProperty("docker.pass")?.toString() ?: System.getenv("DOCKER_PASS")
        }
    }
    container {
        mainClass = "com.github.ApplicationKt"
        jvmFlags = listOf("-Duser.timezone=UTC", "-Djava.security.egd=file:/dev/./urandom")
        ports = listOf("8080")
        creationTime.set("USE_CURRENT_TIMESTAMP")
    }
}