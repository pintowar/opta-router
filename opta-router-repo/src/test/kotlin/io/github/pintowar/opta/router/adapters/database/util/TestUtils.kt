package io.github.pintowar.opta.router.adapters.database.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import io.github.pintowar.opta.router.core.serialization.Serde
import kotlinx.coroutines.reactive.awaitSingle
import org.flywaydb.core.Flyway
import org.jooq.DSLContext
import org.jooq.impl.DSL
import java.lang.reflect.Type

object TestUtils {
    /**
     * Initializes and migrates the H2 database for testing purposes.
     *
     * This function configures Flyway to manage database migrations for an H2 file-based database.
     * It then performs the migrations and returns a [DSLContext] for interacting with the initialized database.
     *
     * @return A [DSLContext] instance connected to the initialized H2 test database.
     */
    fun initDB(): DSLContext {
        val flyway =
            Flyway
                .configure()
                .dataSource("jdbc:h2:file:~/.opta.router/test.h2.db", "sa", "")
                .schemas("PUBLIC")
                .locations("classpath:db/specific/h2")
                .baselineOnMigrate(true)
                .load()
        flyway.migrate()

        return DSL
            .using(
                "r2dbc:h2:file:///~/.opta.router/test.h2.db",
                "sa",
                ""
            )
    }

    /**
     * Runs an initialization SQL script on the provided [DSLContext].
     *
     * This is typically used to populate the test database with initial data before tests.
     *
     * @param dsl The [DSLContext] to execute the script on.
     */
    suspend fun runInitScript(dsl: DSLContext) {
        dsl.query("RUNSCRIPT FROM 'classpath:db/data-h2.sql'").awaitSingle()
    }

    /**
     * Cleans (deletes all data from) the relevant tables in the database.
     *
     * This function is used to ensure a clean state before each test run by deleting all records
     * from the `VEHICLE`, `LOCATION`, `VRP_SOLVER_SOLUTION`, `VRP_SOLVER_REQUEST`, and `VRP_PROBLEM` tables.
     *
     * @param dsl The [DSLContext] to perform the table cleaning operations on.
     */
    suspend fun cleanTables(dsl: DSLContext) {
        dsl.query("DELETE FROM \"PUBLIC\".\"VEHICLE\"").awaitSingle()
        dsl.query("DELETE FROM \"PUBLIC\".\"LOCATION\"").awaitSingle()
        dsl.query("DELETE FROM \"PUBLIC\".\"VRP_SOLVER_SOLUTION\"").awaitSingle()
        dsl.query("DELETE FROM \"PUBLIC\".\"VRP_SOLVER_REQUEST\"").awaitSingle()
        dsl.query("DELETE FROM \"PUBLIC\".\"VRP_PROBLEM\"").awaitSingle()
    }

    /**
     * Provides a pre-configured [ObjectMapper] instance for JSON serialization/deserialization.
     *
     * The mapper is configured for pretty printing, and to disable writing dates and durations as timestamps.
     *
     * @return A configured [ObjectMapper] instance.
     */
    fun mapper(): ObjectMapper =
        ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS)
            .findAndRegisterModules()

    /**
     * Provides a [Serde] implementation for testing, using the configured [ObjectMapper].
     *
     * This implementation supports JSON serialization and deserialization. CBOR methods are not implemented.
     *
     * @return A [Serde] instance for testing.
     */
    fun serde(): Serde =
        object : Serde {
            private val objectMapper = mapper()

            override fun <T : Any> fromJson(
                content: String,
                type: Type
            ): T = objectMapper.readValue(content, objectMapper.constructType(type))

            override fun toJson(value: Any): String = objectMapper.writeValueAsString(value)

            override fun <T : Any> fromCbor(
                content: ByteArray,
                type: Type
            ): T = TODO()

            override fun toCbor(value: Any): ByteArray = TODO()
        }
}