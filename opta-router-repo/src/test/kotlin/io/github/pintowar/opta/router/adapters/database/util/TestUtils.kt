package io.github.pintowar.opta.router.adapters.database.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import io.github.pintowar.opta.router.core.serialization.Serde
import io.github.pintowar.opta.router.core.serialization.TypeRef
import kotlinx.coroutines.reactive.awaitSingle
import org.flywaydb.core.Flyway
import org.jooq.DSLContext
import org.jooq.impl.DSL
import java.lang.reflect.Type

object TestUtils {

    fun initDB(): DSLContext {
        val flyway = Flyway.configure()
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

    suspend fun runInitScript(dsl: DSLContext) {
        dsl.query("RUNSCRIPT FROM 'classpath:db/data-h2.sql'").awaitSingle()
    }

    suspend fun cleanTables(dsl: DSLContext) {
        dsl.query("DELETE FROM \"PUBLIC\".\"VEHICLE\"").awaitSingle()
        dsl.query("DELETE FROM \"PUBLIC\".\"LOCATION\"").awaitSingle()
        dsl.query("DELETE FROM \"PUBLIC\".\"VRP_SOLVER_SOLUTION\"").awaitSingle()
        dsl.query("DELETE FROM \"PUBLIC\".\"VRP_SOLVER_REQUEST\"").awaitSingle()
        dsl.query("DELETE FROM \"PUBLIC\".\"VRP_PROBLEM\"").awaitSingle()
    }

    fun mapper(): ObjectMapper {
        return ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS)
            .findAndRegisterModules()
    }

    fun serde(): Serde = object : Serde {
        private val objectMapper = mapper()

        override fun <T : Any> fromJson(content: String, type: Type): T {
            return objectMapper.readValue(content, objectMapper.constructType(type))
        }

        override fun toJson(value: Any): String {
            return objectMapper.writeValueAsString(value)
        }

        override fun <T : Any> fromCbor(content: ByteArray, type: Type): T = TODO()

        override fun toCbor(value: Any): ByteArray = TODO()
    }
}