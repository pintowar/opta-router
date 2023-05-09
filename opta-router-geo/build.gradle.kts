plugins {
    id("opta-router.base")
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
    implementation(project(":opta-router-core"))
    api(libs.graphhopper.core)

    runtimeOnly(libs.slf4j)
}
