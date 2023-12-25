import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm")
    id("com.diffplug.spotless")
    id("net.saliman.properties")
}

repositories {
    mavenLocal()
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    implementation(libs.bundles.kotlin)
}

tasks {
    kotlin {
        jvmToolchain(21)
    }

    withType<KotlinCompile> {
        compilerOptions {
            freeCompilerArgs.set(listOf("-Xjsr305=strict"))
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    register<Jar>("sourcesJar") {
        archiveClassifier.set("sources")
        archiveExtension.set("jar")
        from(sourceSets["main"].allSource)
    }

    register<Jar>("javadocJar") {
        archiveClassifier.set("javadoc")
        archiveExtension.set("jar")
    }
}

spotless {
    kotlin {
        targetExclude(
            fileTree(project.projectDir) {
                include("build/generated-src/**")
            }
        )
        ktlint()
            .setEditorConfigPath("${rootProject.projectDir}/.editorconfig")
//    licenseHeaderFile()
    }
}