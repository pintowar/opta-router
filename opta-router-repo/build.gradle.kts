import org.jooq.meta.jaxb.ForcedType

plugins {
    id("opta-router.base")
    alias(libs.plugins.flyway)
    alias(libs.plugins.jooq)
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
    implementation(project(":opta-router-core"))
    api(libs.bundles.jooq)
    api(libs.bundles.jackson)

    runtimeOnly(libs.slf4j)
    runtimeOnly(libs.h2.db)
    jooqGenerator(libs.h2.db)
}

flyway {
//    configurations = arrayOf("flywayMigration")
    url = "jdbc:h2:file:/tmp/opta.router.db"
    user = "sa"
    password = ""
}

jooq {
    version.set(libs.versions.jooq.get())
    configurations {
        create("main") {
            jooqConfiguration.apply {
                jdbc.apply {
                    driver = "org.h2.Driver"
                    url = "jdbc:h2:file:/tmp/opta.router.db"
                    user = "sa"
                    password = ""
                }
                generator.apply {
                    name = "org.jooq.codegen.KotlinGenerator"
                    database.apply {
                        name = "org.jooq.meta.h2.H2Database"
                        forcedTypes = listOf(
                            ForcedType().apply {
                                name = "Instant"
                                types = "timestamp"
                            }
                        )
                    }
                    generate.apply {
                        isImplicitJoinPathsAsKotlinProperties = true
//                        isKotlinSetterJvmNameAnnotationsOnIsPrefix = true
                        isPojosAsKotlinDataClasses = true
                        isKotlinNotNullPojoAttributes = true
                        isKotlinNotNullRecordAttributes = true
                        isKotlinNotNullInterfaceAttributes = true
                    }
//                    target.apply {
//                        packageName = "nu.studer.sample"
//                        directory = "src/generated/jooq"
//                    }
                    strategy.name = "org.jooq.codegen.DefaultGeneratorStrategy"
                }
            }
        }
    }
}
