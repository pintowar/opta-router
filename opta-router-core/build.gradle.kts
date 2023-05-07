import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm)
//    alias(libs.plugins.spotless)
    `java-library`
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

    api(libs.bundles.optaplanner) {
        exclude(group = "com.google.protobuf")
        exclude(group = "com.sun.xml.bind")
    }

    runtimeOnly(libs.slf4j)
}

tasks {
    withType<KotlinCompile> {
        compilerOptions {
            freeCompilerArgs.set(listOf("-Xjsr305=strict"))
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
}

//spotless {
//    kotlin {
//        ktlint()
//            .setEditorConfigPath("${rootProject.projectDir}/.editorconfig")
////    licenseHeaderFile()
//    }
//}
