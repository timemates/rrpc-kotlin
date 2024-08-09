@file:OptIn(ExperimentalSerializationApi::class, ExperimentalInterceptorsApi::class)
@file:Suppress("UNCHECKED_CAST")

package org.timemates.rsp.client

import io.ktor.utils.io.core.*
import io.rsocket.kotlin.payload.Payload
import kotlinx.coroutines.flow.*
import kotlinx.serialization.*
import org.timemates.rsp.DataVariant
import org.timemates.rsp.annotations.ExperimentalInterceptorsApi
import org.timemates.rsp.annotations.InternalRSProtoAPI
import org.timemates.rsp.client.config.RSPClientConfig
import org.timemates.rsp.instances.protobuf
import org.timemates.rsp.interceptors.InterceptorContext
import org.timemates.rsp.metadata.ClientMetadata
import org.timemates.rsp.metadata.ServerMetadata
import org.timemates.rsp.options.Options
import org.timemates.rsp.requireSingle
import org.timemates.rsp.requireStreaming

/**
 * Handles client requests by managing metadata and data serialization, making the appropriate calls, and processing the responses.
 * Utilizes interceptors to modify request and response data.
 *
 * @property config The configuration for the RSP client.
 */
@InternalRSProtoAPI
public class ClientRequestHandler(
    private val config: RSPClientConfig,
) {
    private val protobuf get() = config.instances.protobuf ?: error("Protobuf instance should always be present.")

    /**
     * Makes a request-response call.
     *
     * @param metadata Metadata to be sent with the request.
     * @param data Data to be sent with the request.
     * @param options Options for the request.
     * @param serializationStrategy Serialization strategy for the data.
     * @param deserializationStrategy Deserialization strategy for the response data.
     * @return The response data.
     */
    @OptIn(InternalRSProtoAPI::class)
    public suspend fun <T : Any, R : Any> requestResponse(
        metadata: ClientMetadata,
        data: T,
        options: Options,
        serializationStrategy: SerializationStrategy<T>,
        deserializationStrategy: DeserializationStrategy<R>,
    ): R = with(config) {
        val requestContext = interceptors.runInputInterceptors(
            DataVariant.Single(data),
            metadata,
            options,
            instances,
        )

        val finalMetadata = requestContext?.metadata ?: metadata
        val finalData = (requestContext?.data?.requireSingle() as? T) ?: data

        val request = Payload(
            ByteReadPacket(protobuf.encodeToByteArray(serializationStrategy, value = finalData)),
            ByteReadPacket(protobuf.encodeToByteArray(ClientMetadata.serializer(), finalMetadata)),
        )

        val response = try {
            rsocket.requestResponse(request)
                .let {
                    it.metadata?.readBytes()?.let { bytes ->
                        protobuf.decodeFromByteArray<ServerMetadata>(bytes)
                    }!! to protobuf.decodeFromByteArray(deserializationStrategy, it.data.readBytes())
                }
        } catch (e: Exception) {
            e
        }

        if (interceptors.response.isNotEmpty()) {
            val result = interceptors.response.fold(
                InterceptorContext(
                    data = when (response) {
                        is Exception -> DataVariant.Failure(response)
                        is Pair<*, *> -> DataVariant.Single(response.second as T)
                        else -> error("Should not reach here.")
                    },
                    metadata = (response as? Pair<*, *>)?.first as ServerMetadata,
                    options = options,
                    instances = requestContext?.instances ?: instances,
                )
            ) { acc, interceptor ->
                interceptor.intercept(acc)
            }

            return if (result.data is DataVariant.Failure)
                throw (result.data as DataVariant.Failure<*>).exception
            else result.data.requireSingle() as R
        }

        return when (response) {
            is Exception -> throw response
            is Pair<*, *> -> response.second as R
            else -> error("Should not reach here.")
        }
    }

    /**
     * Makes a request-stream call.
     *
     * @param metadata Metadata to be sent with the request.
     * @param data Data to be sent with the request.
     * @param options Options for the request.
     * @param serializationStrategy Serialization strategy for the data.
     * @param deserializationStrategy Deserialization strategy for the response data.
     * @return A flow of the response data.
     */
    @OptIn(InternalRSProtoAPI::class)
    public fun <T : Any, R : Any> requestStream(
        metadata: ClientMetadata,
        data: T,
        options: Options,
        serializationStrategy: SerializationStrategy<T>,
        deserializationStrategy: DeserializationStrategy<R>,
    ): Flow<R> = with(config) {
        val requestContext = interceptors.runInputInterceptors(
            DataVariant.Single(data),
            metadata,
            options,
            instances,
        )

        val finalMetadata = requestContext?.metadata ?: metadata
        val finalData = (requestContext?.data?.requireSingle() as? T) ?: data

        val request = Payload(
            ByteReadPacket(protobuf.encodeToByteArray(serializationStrategy, value = finalData)),
            ByteReadPacket(protobuf.encodeToByteArray(ClientMetadata.serializer(), finalMetadata)),
        )

        return@with handleStreamingResponse(
            rsocket.requestStream(request),
            options,
            requestContext,
            deserializationStrategy,
        )
    }

    /**
     * Makes a request-channel call.
     *
     * @param metadata Metadata to be sent with the request.
     * @param data A flow of data to be sent with the request.
     * @param options Options for the request.
     * @param serializationStrategy Serialization strategy for the data.
     * @param deserializationStrategy Deserialization strategy for the response data.
     * @return A flow of the response data.
     */
    @OptIn(InternalRSProtoAPI::class)
    public fun <T : Any, R : Any> requestChannel(
        metadata: ClientMetadata,
        data: Flow<T>,
        options: Options,
        serializationStrategy: SerializationStrategy<T>,
        deserializationStrategy: DeserializationStrategy<R>,
    ): Flow<R> = with(config) {
        val requestContext = interceptors.runInputInterceptors(
            DataVariant.Streaming(data),
            metadata,
            options,
            instances,
        )

        return@with handleStreamingResponse(
            response = rsocket.requestChannel(
                // in the initial payload, we put only metadata: it follows up the same logic
                // how we treat responses (the first chunk contains only metadata) and
                // makes it more idiomatic from the ProtoBuf RPC definition side.
                initPayload = Payload(
                    data = ByteReadPacket.Empty,
                    metadata = ByteReadPacket(protobuf.encodeToByteArray<ClientMetadata>(metadata))
                ),
                payloads = (requestContext?.data?.requireStreaming() ?: data)
                    .map { Payload(ByteReadPacket(protobuf.encodeToByteArray(serializationStrategy, it as T))) },
            ),
            options = options,
            requestContext = requestContext,
            deserializationStrategy = deserializationStrategy
        )
    }

    /**
     * Handles streaming responses, applying necessary response interceptors.
     *
     * @param response The flow of payloads from the server.
     * @param options Options for the request.
     * @param requestContext The context of the initial request.
     * @param deserializationStrategy Deserialization strategy for the response data.
     * @return A flow of the response data.
     */
    private fun <R : Any> handleStreamingResponse(
        response: Flow<Payload>,
        options: Options,
        requestContext: InterceptorContext<ClientMetadata>?,
        deserializationStrategy: DeserializationStrategy<R>,
    ): Flow<R> = with(config) {
        if (interceptors.response.isEmpty()) {
            return response.drop(1).map { protobuf.decodeFromByteArray(deserializationStrategy, it.data.readBytes()) }
        }

        return flow {
            // the first element is always metadata-only
            val serverMetadata: ServerMetadata = protobuf.decodeFromByteArray(
                response.first().metadata?.readBytes() ?: noMetadataError()
            )

            val context = interceptors.runOutputInterceptors(
                DataVariant.Streaming(
                    response.map {
                        protobuf.decodeFromByteArray(deserializationStrategy, it.data.readBytes())
                    }
                ),
                serverMetadata,
                options,
                requestContext?.instances ?: instances,
            )

            emitAll(context!!.data.requireStreaming() as Flow<R>)
        }
    }

    /**
     * Throws an error indicating that metadata is required but not present.
     */
    private fun noMetadataError(): Nothing =
        error("No metadata was present in the response, but was a requirement. Please report.")
}
