import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.0.6"
    id("io.spring.dependency-management") version "1.1.0"
    kotlin("jvm") version "1.8.20"
    kotlin("plugin.spring") version "1.8.20"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.set(listOf("-Xjsr305=strict"))
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

repositories {
    mavenCentral()
}

val optaplannerVersion = "9.37.0.Final"
val ghVersion = "7.0"
val kloggingVersion = "1.5.9"

dependencies {
    implementation("org.optaplanner:optaplanner-examples:$optaplannerVersion") {
        exclude(group = "com.google.protobuf")
        exclude(group = "com.sun.xml.bind")
    }

    implementation("com.graphhopper:graphhopper-core:$ghVersion")

    implementation("org.springframework.boot:spring-boot-starter-websocket") {
        exclude(module = "spring-boot-starter-logging")
    }
    implementation("org.springframework.boot:spring-boot-actuator")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("io.github.microutils:kotlin-logging:$kloggingVersion")

    implementation("com.fasterxml.jackson.core:jackson-annotations")
    implementation("com.fasterxml.jackson.core:jackson-core")
    implementation("com.fasterxml.jackson.core:jackson-databind")
//    runtime "com.fasterxml.jackson.datatype:jackson-datatype-jdk8"
//    runtime "com.fasterxml.jackson.datatype:jackson-datatype-jsr310"
    runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.2")
    runtimeOnly("org.slf4j:jcl-over-slf4j:1.7.25")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
