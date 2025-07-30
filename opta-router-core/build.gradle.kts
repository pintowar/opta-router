plugins {
    id("opta-router.base")
    alias(libs.plugins.dokka)
    `java-library`
    `java-test-fixtures`
}

dependencies {
    api(libs.kotlin.coroutines.core)

    runtimeOnly(libs.slf4j)
}