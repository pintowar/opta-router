package io.github.pintowar.opta.router.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper
import io.github.pintowar.opta.router.core.serialization.Serde
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.lang.reflect.Type

@Configuration
class SerdeConfig {

    companion object {
        fun cborMapperFromObjectMapper(objectMapper: ObjectMapper): ObjectMapper =
            CBORMapper().findAndRegisterModules().apply {
                SerializationFeature.entries.forEach {
                    this.configure(it, objectMapper.serializationConfig.isEnabled(it))
                }
                DeserializationFeature.entries.forEach {
                    this.configure(it, objectMapper.deserializationConfig.isEnabled(it))
                }
            }
    }

    @Bean
    fun serde(objectMapper: ObjectMapper) = object : Serde {
        private val cborMapper = cborMapperFromObjectMapper(objectMapper)

        override fun <T : Any> fromJson(content: String, type: Type): T {
            return objectMapper.readValue(content, objectMapper.constructType(type))
        }

        override fun toJson(value: Any): String {
            return objectMapper.writeValueAsString(value)
        }

        override fun <T : Any> fromCbor(content: ByteArray, type: Type): T {
            return cborMapper.readValue(content, cborMapper.constructType(type))
        }

        override fun toCbor(value: Any): ByteArray {
            return cborMapper.writeValueAsBytes(value)
        }
    }
}