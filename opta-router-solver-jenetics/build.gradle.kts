plugins {
    id("opta-router.base")
    `java-library`
}

dependencies {
    implementation(project(":opta-router-core"))
    api(libs.jenetics)

    runtimeOnly(libs.slf4j)
}