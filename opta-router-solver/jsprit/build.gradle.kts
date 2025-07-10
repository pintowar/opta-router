plugins {
    id("opta-router.base")
    `java-library`
}

dependencies {
    implementation(project(":opta-router-core"))
    api(libs.jsprit.core)

    runtimeOnly(libs.slf4j)

    testImplementation(testFixtures(project(":opta-router-core")))
}