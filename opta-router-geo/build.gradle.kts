plugins {
    id("opta-router.base")
    alias(libs.plugins.dokka)
    `java-library`
}

dependencies {
    implementation(project(":opta-router-core"))
    api(libs.graphhopper.core)

    runtimeOnly(libs.slf4j)
}