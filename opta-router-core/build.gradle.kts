plugins {
    id("opta-router.base")
    `java-library`
}

dependencies {
    api(libs.kotlin.coroutines.core)

    runtimeOnly(libs.slf4j)
}