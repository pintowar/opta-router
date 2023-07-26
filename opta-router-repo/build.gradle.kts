import org.jooq.meta.jaxb.ForcedType

plugins {
    id("opta-router.base")
    alias(libs.plugins.flyway)
    alias(libs.plugins.jooq)
    `java-library`
}

dependencies {
    implementation(project(":opta-router-core"))
    api(libs.bundles.jooq)
    api(libs.bundles.jackson)

    runtimeOnly(libs.slf4j)
    runtimeOnly(if (project.isDistProfile) libs.pg.db else libs.h2.db)
    jooqGenerator(if (project.isDistProfile) libs.pg.db else libs.h2.db)
}

tasks.flywayMigrate {
    dependsOn("processResources")
}

jooq {
    version.set(libs.versions.jooq.get())
    configurations {
        create("main") {
            jooqConfiguration.apply {
                jdbc.apply {
                    driver = project.property("flyway.driver").toString()
                    url = project.property("flyway.url").toString()
                    user = project.property("flyway.user").toString()
                    password = project.property("flyway.password").toString()
                }
                generator.apply {
                    name = "org.jooq.codegen.KotlinGenerator"
                    database.apply {
                        name = project.property("jooq.generator").toString()
                        inputSchema = project.property("flyway.defaultSchema").toString()
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
                        isRoutines = false
                        isDeprecated = false
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

tasks.named<nu.studer.gradle.jooq.JooqGenerate>("generateJooq") {
    // ensure database schema has been prepared by Flyway before generating the jOOQ sources
    dependsOn("flywayMigrate")

    // declare Flyway migration scripts as inputs on the jOOQ task
    inputs.files(fileTree("${project.projectDir}/src/main/resources/db/migration"))
        .withPropertyName("migrations")
        .withPathSensitivity(PathSensitivity.RELATIVE)

    // make jOOQ task participate in incremental builds and build caching
    allInputsDeclared.set(true)
    outputs.cacheIf { true }
}