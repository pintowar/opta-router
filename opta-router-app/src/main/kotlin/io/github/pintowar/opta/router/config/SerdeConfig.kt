package io.github.pintowar.opta.router.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper
import io.github.pintowar.opta.router.core.serialization.Serde
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.lang.reflect.Type

/**
 * This class is responsible for configuring the serialization and deserialization.
 */
@Configuration
class SerdeConfig {
    companion object {
        /**
         * Creates a CBOR mapper from an Object mapper.
         *
         * @param objectMapper The Object mapper.
         * @return The CBOR mapper.
         */
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

    /**
     * Creates a Serde bean.
     *
     * @param objectMapper The Object mapper.
     * @return The Serde bean.
     */
    @Bean
    fun serde(objectMapper: ObjectMapper) =
        object : Serde {
            private val cborMapper = cborMapperFromObjectMapper(objectMapper)

            override fun <T : Any> fromJson(
                content: String,
                type: Type
            ): T = objectMapper.readValue(content, objectMapper.constructType(type))

            override fun toJson(value: Any): String = objectMapper.writeValueAsString(value)

            override fun <T : Any> fromCbor(
                content: ByteArray,
                type: Type
            ): T = cborMapper.readValue(content, cborMapper.constructType(type))

            override fun toCbor(value: Any): ByteArray = cborMapper.writeValueAsBytes(value)
        }
}