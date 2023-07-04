plugins {
    id("opta-router.base")
    `java-library`
}

dependencies {
    implementation(project(":opta-router-core"))
    api(libs.bundles.timefold) {
        exclude(group = "com.google.protobuf")
        exclude(group = "com.sun.xml.bind")
    }

    runtimeOnly(libs.slf4j)
}