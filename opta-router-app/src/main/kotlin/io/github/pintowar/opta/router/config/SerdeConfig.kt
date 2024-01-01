package io.github.pintowar.opta.router.config

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.pintowar.opta.router.core.serialization.Serde
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.lang.reflect.Type

@Configuration
class SerdeConfig {

    @Bean
    fun serde(objectMapper: ObjectMapper) = object : Serde {

        override fun <T : Any> fromJson(content: String, type: Type): T {
            return objectMapper.readValue(content, objectMapper.constructType(type))
        }

        override fun toJson(value: Any): String {
            return objectMapper.writeValueAsString(value)
        }
    }
}