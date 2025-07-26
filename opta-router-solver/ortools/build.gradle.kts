plugins {
    id("opta-router.base")
    alias(libs.plugins.dokka)
    `java-library`
}

dependencies {
    implementation(project(":opta-router-core"))
    api(libs.or.tools)

    runtimeOnly(libs.slf4j)

    testImplementation(testFixtures(project(":opta-router-core")))
}