plugins {
    id("opta-router.base")
    alias(libs.plugins.dokka)
    `java-library`
}

dependencies {
    implementation(project(":opta-router-core"))
    api(libs.bundles.optaplanner) {
        exclude(group = "com.google.protobuf")
        exclude(group = "com.sun.xml.bind")
    }

    runtimeOnly(libs.slf4j)

    testImplementation(testFixtures(project(":opta-router-core")))
}