plugins {
    id("opta-router.base")
    `java-library`
    `java-test-fixtures`
}

dependencies {
    api(libs.kotlin.coroutines.core)

    runtimeOnly(libs.slf4j)
}