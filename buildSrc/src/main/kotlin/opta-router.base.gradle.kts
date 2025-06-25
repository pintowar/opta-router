import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm")
    jacoco
    id("com.diffplug.spotless")
    id("net.saliman.properties")
    id("com.github.ben-manes.versions")
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
    testImplementation(libs.bundles.kotest) {
        exclude(group = "org.jetbrains.kotlinx")
    }
    testImplementation(libs.kotlin.coroutines.test)
}

tasks {
    kotlin {
        jvmToolchain(21)
    }

    withType<Test>().configureEach {
        useJUnitPlatform()
        finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
    }

    named<JacocoReport>("jacocoTestReport") {
        reports {
            xml.required.set(true)
            html.required.set(true)
        }

        classDirectories.setFrom(
            files(
                classDirectories.files.map {
                    fileTree(it) {
                        exclude(excludedJacocoPackages)
                    }
                }
            )
        )
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

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
        }
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