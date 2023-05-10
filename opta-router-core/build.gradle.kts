plugins {
    id("opta-router.base")
    `java-library`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    api(libs.bundles.optaplanner) {
        exclude(group = "com.google.protobuf")
        exclude(group = "com.sun.xml.bind")
    }

    runtimeOnly(libs.slf4j)
}