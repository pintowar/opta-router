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
    runtimeOnly(if (project.isProdProfile) libs.pg.db else libs.h2.db)
    jooqGenerator(if (project.isProdProfile) libs.pg.db else libs.h2.db)
}

flyway {
//    configurations = arrayOf("flywayMigration")
    driver = if (project.isProdProfile) "org.postgresql.Driver" else "org.h2.Driver"
    url = if (project.isProdProfile) "jdbc:postgresql://localhost:5432/opta-router" else "jdbc:h2:file:~/.opta.router/h2.db"
    user = if (project.isProdProfile) "postgres" else "sa"
    password = if (project.isProdProfile) "postgres" else ""
    locations = arrayOf("classpath:db/migration", "classpath:db/specific/${if (project.isProdProfile) "postgres" else "h2"}")
}

jooq {
    version.set(libs.versions.jooq.get())
    configurations {
        create("main") {
            jooqConfiguration.apply {
                jdbc.apply {
                    driver = flyway.driver
                    url = flyway.url
                    user = flyway.user
                    password = flyway.password
                }
                generator.apply {
                    name = "org.jooq.codegen.KotlinGenerator"
                    database.apply {
                        val (pg, h2) = "org.jooq.meta.postgres.PostgresDatabase" to "org.jooq.meta.h2.H2Database"
                        name = if (project.isProdProfile) pg else h2
                        inputSchema = if (project.isProdProfile) "public" else "PUBLIC"
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