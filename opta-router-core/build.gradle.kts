plugins {
    id("opta-router.base")
    `java-library`
}

dependencies {
    api(libs.bundles.optaplanner) {
        exclude(group = "com.google.protobuf")
        exclude(group = "com.sun.xml.bind")
    }

    runtimeOnly(libs.slf4j)
}