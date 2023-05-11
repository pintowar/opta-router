plugins {
    id("opta-router.base")
    `java-library`
}

dependencies {
    implementation(project(":opta-router-core"))
    api(libs.graphhopper.core)

    runtimeOnly(libs.slf4j)
}