@file:Suppress("MemberVisibilityCanBePrivate")

package com.y9vad9.rsocket.proto.requests

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.serializer
import kotlin.reflect.typeOf

/**
 * Represents a procedure descriptor for a remote method call.
 *
 * @param T The type of the request object.
 * @param R The type of the response object.
 * @property name The name of the method.
 * @property kind The kind of request.
 * @property requestSerializer The serializer for the request object.
 * @property returnSerializer The serializer for the response object.
 */
public class ProcedureDescriptor<T : Any, R : Any>(
    public val name: String,
    public val kind: RequestKind,
    public val requestSerializer: KSerializer<T>,
    public val returnSerializer: KSerializer<R>,
    public val procedure: suspend (T) -> R,
) {
    public companion object {
        /**
         * Creates a procedure descriptor for a remote method call.
         *
         * @param name The name of the method.
         * @param kind The kind of request.
         * @return The method descriptor.
         * @param T The type of the request object.
         * @param R The type of the response object.
         * @throws SerializationException If the request or response type does not have a serializer.
         */
        public inline fun <reified T : Any, reified R : Any> of(
            name: String,
            kind: RequestKind,
            noinline procedure: suspend (T) -> R,
        ): ProcedureDescriptor<T, R> {
            @Suppress("UNCHECKED_CAST")
            return ProcedureDescriptor(
                name = name,
                kind = kind,
                requestSerializer = serializer(typeOf<T>()) as KSerializer<T>,
                returnSerializer = serializer(typeOf<R>()) as KSerializer<R>,
                procedure = procedure,
            )
        }
    }
}