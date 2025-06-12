package io.github.pintowar.opta.router.core.serialization

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

interface Serde {
    fun <T : Any> fromJson(
        content: String,
        type: Type
    ): T

    fun <T : Any> fromJson(
        content: String,
        ref: TypeRef<T>
    ): T = fromJson(content, ref.type)

    fun toJson(value: Any): String

    fun <T : Any> fromCbor(
        content: ByteArray,
        type: Type
    ): T

    fun <T : Any> fromCbor(
        content: ByteArray,
        ref: TypeRef<T>
    ): T = fromCbor(content, ref.type)

    fun toCbor(value: Any): ByteArray
}

inline fun <reified T : Any> Serde.fromJson(content: String): T = fromJson(content, object : TypeRef<T>() {})

inline fun <reified T : Any> Serde.fromCbor(content: ByteArray): T = fromCbor(content, object : TypeRef<T>() {})

abstract class TypeRef<T> protected constructor() : Comparable<TypeRef<T>> {
    val type: Type by lazy {
        val superClass: Type = javaClass.genericSuperclass
        require(superClass !is Class<*>) { // sanity check, should never happen
            "Internal error: TypeReference constructed without actual type information"
        }

        (superClass as ParameterizedType).actualTypeArguments[0]
    }

    override fun compareTo(other: TypeRef<T>): Int {
        return 0
    }
}