package io.github.pintowar.opta.router.core.serialization

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * The Serde interface provides methods for serializing and deserializing objects to and from JSON and CBOR formats.
 */
interface Serde {
    /**
     * Deserializes a JSON string into an object of the specified type.
     *
     * @param content The JSON string to deserialize.
     * @param type The type of the object to deserialize into.
     * @return The deserialized object.
     */
    fun <T : Any> fromJson(
        content: String,
        type: Type
    ): T

    /**
     * Deserializes a JSON string into an object of the specified type, using a [TypeRef] to capture generic type information.
     *
     * @param content The JSON string to deserialize.
     * @param ref A [TypeRef] that captures the type of the object to deserialize into.
     * @return The deserialized object.
     */
    fun <T : Any> fromJson(
        content: String,
        ref: TypeRef<T>
    ): T = fromJson(content, ref.type)

    /**
     * Serializes an object into a JSON string.
     *
     * @param value The object to serialize.
     * @return The JSON string representation of the object.
     */
    fun toJson(value: Any): String

    /**
     * Deserializes a CBOR byte array into an object of the specified type.
     *
     * @param content The CBOR byte array to deserialize.
     * @param type The type of the object to deserialize into.
     * @return The deserialized object.
     */
    fun <T : Any> fromCbor(
        content: ByteArray,
        type: Type
    ): T

    /**
     * Deserializes a CBOR byte array into an object of the specified type, using a [TypeRef] to capture generic type information.
     *
     * @param content The CBOR byte array to deserialize.
     * @param ref A [TypeRef] that captures the type of the object to deserialize into.
     * @return The deserialized object.
     */
    fun <T : Any> fromCbor(
        content: ByteArray,
        ref: TypeRef<T>
    ): T = fromCbor(content, ref.type)

    /**
     * Serializes an object into a CBOR byte array.
     *
     * @param value The object to serialize.
     * @return The CBOR byte array representation of the object.
     */
    fun toCbor(value: Any): ByteArray
}

/**
 * Extension function to deserialize a JSON string into an object of the reified type.
 *
 * @param content The JSON string to deserialize.
 * @return The deserialized object.
 */
inline fun <reified T : Any> Serde.fromJson(content: String): T = fromJson(content, object : TypeRef<T>() {})

/**
 * Extension function to deserialize a CBOR byte array into an object of the reified type.
 *
 * @param content The CBOR byte array to deserialize.
 * @return The deserialized object.
 */
inline fun <reified T : Any> Serde.fromCbor(content: ByteArray): T = fromCbor(content, object : TypeRef<T>() {})

/**
 * Abstract class used to capture generic type information.
 */
abstract class TypeRef<T> protected constructor() : Comparable<TypeRef<T>> {
    val type: Type by lazy {
        val superClass: Type = javaClass.genericSuperclass
        require(superClass !is Class<*>) {
            // sanity check, should never happen
            "Internal error: TypeReference constructed without actual type information"
        }

        (superClass as ParameterizedType).actualTypeArguments[0]
    }

    /**
     * Compares this TypeRef with another TypeRef.
     *
     * @param other The other TypeRef to compare with.
     * @return Always returns 0.
     */
    override fun compareTo(other: TypeRef<T>): Int = 0
}