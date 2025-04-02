@file:OptIn(ExperimentalSerializationApi::class, ExperimentalInterceptorsApi::class, InternalRRpcAPI::class)

package org.timemates.rrpc.server.module

import io.ktor.utils.io.core.*
import io.rsocket.kotlin.RSocketError
import io.rsocket.kotlin.RSocketRequestHandlerBuilder
import io.rsocket.kotlin.payload.Payload
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import org.timemates.rrpc.*
import org.timemates.rrpc.annotations.ExperimentalInterceptorsApi
import org.timemates.rrpc.annotations.InternalRRpcAPI
import org.timemates.rrpc.exceptions.ProcedureNotFoundException
import org.timemates.rrpc.exceptions.ServiceNotFoundException
import org.timemates.rrpc.instances.InstanceContainer
import org.timemates.rrpc.instances.ProtobufInstance
import org.timemates.rrpc.interceptors.InterceptorContext
import org.timemates.rrpc.interceptors.Interceptors
import org.timemates.rrpc.metadata.ClientMetadata
import org.timemates.rrpc.metadata.ServerMetadata
import org.timemates.rrpc.options.OptionsWithValue
import org.timemates.rrpc.server.RequestContext
import org.timemates.rrpc.server.module.descriptors.ProcedureDescriptor
import org.timemates.rrpc.server.module.descriptors.ServiceDescriptor
import org.timemates.rrpc.server.module.descriptors.procedure
import org.timemates.rrpc.server.toRequestContext

/**
 * Handler class for setting up RSocket request handlers.
 *
 * @param module The RRpcModule instance to handle requests.
 */
@Suppress("DuplicatedCode")
public class RRpcModuleHandler(private val module: RRpcModule) {
    private val services = module.services.associateBy { it.name }
    private val protobuf = module.getInstance(ProtobufInstance)!!.protobuf
    private val serverMetadata = ServerMetadata.EMPTY

    /**
     * Sets up the RSocketRequestHandler with the necessary request handlers.
     *
     * @param builder The RSocketRequestHandlerBuilder to set up.
     */
    public fun setup(builder: RSocketRequestHandlerBuilder) {
        builder.apply {
            requestResponseHandler()
            requestStreamHandler()
            requestChannelHandler()
            fireAndForgetHandler()
            metadataPushHandler()
        }
    }

    private fun RSocketRequestHandlerBuilder.requestResponseHandler() {
        requestResponse { payload ->
            val moduleInstances = module.plus(RequestType.REQUEST_RESPONSE)

            val metadata = getClientMetadata(
                payload.metadataOrFailure(),
                module.interceptors,
                moduleInstances,
            )
            val service = getService(metadata)
            val method =
                service.procedure<ProcedureDescriptor.RequestResponse<ProtoType, ProtoType>>(metadata.procedureName)
                    ?: handleException(ProcedureNotFoundException(metadata), null)

            val options = method.options

            // Decode the input data
            val data = try {
                Single(protobuf.decodeFromByteArray(method.inputSerializer, payload.data.readBytes()))
            } catch (e: Exception) {
                handleException(e, method)
            }

            // Run input interceptors and handle exceptions
            val startContext = module.interceptors.runInputInterceptors(
                data = data,
                clientMetadata = metadata,
                options = options,
                instanceContainer = moduleInstances,
            )

            if (startContext?.data is Failure) throw (startContext.data as Failure).exception


            // Execute the method and handle exceptions
            val result = try {
                method.execute(
                    context = startContext?.toRequestContext() ?: RequestContext(module, metadata, options),
                    input = (startContext?.data ?: data).requireSingle()
                )
            } catch (e: Exception) {
                handleException(e, method, startContext)
            }

            // Run output interceptors and handle exceptions
            val finalContext = module.interceptors.runOutputInterceptors(
                data = Single(result),
                serverMetadata = serverMetadata,
                options = startContext?.options ?: options,
                instanceContainer = startContext?.instances ?: moduleInstances,
            )

            if (finalContext?.data is Failure) throw (finalContext.data as Failure).exception

            ((finalContext?.data as? Single)?.value ?: result).toPayload(
                strategy = method.outputSerializer, serverMetadata = serverMetadata
            )
        }
    }

    private fun RSocketRequestHandlerBuilder.requestStreamHandler() {
        requestStream { payload ->
            val moduleInstances = module.plus(RequestType.REQUEST_STREAM)

            val metadata = getClientMetadata(
                payload.metadataOrFailure(),
                module.interceptors,
                moduleInstances,
            )

            val service = getService(metadata)
            val method =
                service.procedure<ProcedureDescriptor.RequestStream<ProtoType, ProtoType>>(metadata.procedureName)
                    ?: handleException(ProcedureNotFoundException(metadata), null)

            val options = method.options

            // Decode the input data
            val data = try {
                Single(protobuf.decodeFromByteArray(method.inputSerializer, payload.data.readBytes()))
            } catch (e: Exception) {
                handleException(e, method)
            }

            // Run input interceptors and handle exceptions
            val startContext = module.interceptors.runInputInterceptors(
                data = data,
                clientMetadata = metadata,
                options = options,
                moduleInstances,
            )

            if (startContext?.data is Failure)
                throw (startContext.data as Failure).exception

            // Execute the method and handle exceptions
            val result = try {
                method.execute(
                    context = startContext?.toRequestContext() ?: RequestContext(module, metadata, options),
                    value = (startContext?.data ?: data).requireSingle()
                )
            } catch (e: Exception) {
                handleException(e, method, startContext)
            }

            val finalContext = module.interceptors.runOutputInterceptors(
                data = Streaming(result),
                serverMetadata = serverMetadata,
                options = startContext?.options ?: options,
                instanceContainer = startContext?.instances ?: moduleInstances,
            )

            if (finalContext?.data is Failure) throw (finalContext.data as Failure).exception

            ((finalContext?.data as? Streaming)?.flow ?: result).mapToPayload(
                serverMetadata = serverMetadata, strategy = method.outputSerializer
            )
        }
    }


    private fun RSocketRequestHandlerBuilder.requestChannelHandler() {
        requestChannel { initial, payloads ->
            val moduleInstances = module.plus(RequestType.REQUEST_CHANNEL)

            val metadata = getClientMetadata(
                initial.metadataOrFailure(),
                module.interceptors,
                moduleInstances,
            )

            val service = getService(metadata)
            val method =
                service.procedure<ProcedureDescriptor.RequestChannel<ProtoType, ProtoType>>(metadata.procedureName)
                    ?: handleException(ProcedureNotFoundException(metadata), null)

            val options = method.options

            // Decode the input data
            val data = try {
                Streaming(payloads.map {
                    protobuf.decodeFromByteArray(method.inputSerializer, it.data.readBytes())
                })
            } catch (e: Exception) {
                handleException(e, method)
            }

            // Run input interceptors and handle exceptions
            val startContext =
                module.interceptors.runInputInterceptors(
                    data = data,
                    clientMetadata = metadata,
                    options = options,
                    moduleInstances,
                )

            if (startContext?.data is Failure) throw (startContext.data as Failure).exception

            // Execute the method and handle exceptions
            val result = try {
                method.execute(
                    context = startContext?.toRequestContext() ?: RequestContext(module, metadata, options),
                    flow = (startContext?.data ?: data).requireStreaming()
                )
            } catch (e: Exception) {
                handleException(e, method, startContext)
            }

            // Run output interceptors and handle exceptions
            val finalContext = module.interceptors.runOutputInterceptors(
                data = Streaming(result),
                serverMetadata = serverMetadata,
                options = startContext?.options ?: options,
                instanceContainer = startContext?.instances ?: moduleInstances,
            )

            if (finalContext?.data is Failure) throw (finalContext.data as Failure).exception

            ((finalContext?.data as? Streaming)?.flow ?: result).mapToPayload(
                serverMetadata = serverMetadata, strategy = method.outputSerializer
            )
        }
    }

    private fun RSocketRequestHandlerBuilder.fireAndForgetHandler(): Unit = fireAndForget { payload ->
        val moduleInstances = module.plus(RequestType.FIRE_AND_FORGET)

        val metadata = getClientMetadata(
            payload.metadataOrFailure(),
            module.interceptors,
            moduleInstances,
        )

        val service = getService(metadata)
        val method = service.procedure<ProcedureDescriptor.FireAndForget<ProtoType>>(metadata.procedureName)
            ?: handleException(ProcedureNotFoundException(metadata), null)

        val options = method.options

        // Decode the input data
        val data = try {
            Single(protobuf.decodeFromByteArray(method.inputSerializer, payload.data.readBytes()))
        } catch (e: Exception) {
            handleException(e, method)
        }

        // Run input interceptors and handle exceptions
        val startContext = module.interceptors.runInputInterceptors(
            data = data,
            clientMetadata = metadata,
            options = options,
            moduleInstances,
        )

        if (startContext?.data is Failure) throw (startContext.data as Failure).exception


        // Execute the method and handle exceptions
        try {
            method.execute(
                context = startContext?.toRequestContext() ?: RequestContext(module, metadata, options),
                input = (startContext?.data ?: data).requireSingle()
            )
        } catch (e: Exception) {
            handleException(e, method, startContext)
        }

        // Run output interceptors and handle exceptions
        val finalContext = module.interceptors.runOutputInterceptors(
            data = Single.EMPTY,
            serverMetadata = serverMetadata,
            options = startContext?.options ?: options,
            instanceContainer = startContext?.instances ?: moduleInstances,
        )

        // is not propagated to the client
        if (finalContext?.data is Failure) throw (finalContext.data as Failure).exception
    }

    private fun RSocketRequestHandlerBuilder.metadataPushHandler(): Unit = metadataPush { metadataBytes ->
        val moduleInstances = module.plus(RequestType.METADATA_PUSH)

        val metadata = getClientMetadata(
            metadataBytes.readBytes(),
            module.interceptors,
            moduleInstances,
        )

        val service = getService(metadata)
        val method = service.procedure<ProcedureDescriptor.MetadataPush>(metadata.procedureName)
            ?: handleException(ProcedureNotFoundException(metadata), null)

        val options = method.options

        val startContext = module.interceptors.runInputInterceptors(
            data = Single.EMPTY,
            clientMetadata = metadata,
            options = options,
            instanceContainer = moduleInstances,
        )

        if (startContext?.data is Failure) throw (startContext.data as Failure).exception

        try {
            method.execute(
                context = startContext?.toRequestContext() ?: RequestContext(module, metadata, options),
            )
        } catch (e: Exception) {
            handleException(e, method, startContext)
        }

        // Run output interceptors and handle exceptions
        val finalContext = module.interceptors.runOutputInterceptors(
            data = Single.EMPTY,
            serverMetadata = serverMetadata,
            options = startContext?.options ?: options,
            instanceContainer = startContext?.instances ?: moduleInstances,
        )

        // is not propagated to the client
        if (finalContext?.data is Failure) throw (finalContext.data as Failure).exception
    }

    @OptIn(InternalRRpcAPI::class)
    private suspend fun handleException(
        exception: Exception,
        method: ProcedureDescriptor?,
        prevContext: InterceptorContext<ClientMetadata>? = null,
    ): Nothing {
        val resultException = try {
            module.interceptors.runOutputInterceptors(
                data = Failure(exception),
                serverMetadata = serverMetadata,
                options = prevContext?.options ?: method?.options ?: OptionsWithValue.EMPTY,
                instanceContainer = prevContext?.instances ?: module,
            )?.data?.requireFailure() ?: exception
        } catch (e: Exception) {
            // todo special logging
            e
        }

        throw resultException
    }

    private suspend fun getClientMetadata(
        metadata: ByteArray,
        interceptors: Interceptors,
        instances: InstanceContainer,
    ): ClientMetadata {
        return try {
            protobuf.decodeFromByteArray(metadata)
        } catch (e: Exception) {
            interceptors.runOutputInterceptors(
                data = Failure(e),
                serverMetadata = serverMetadata,
                options = OptionsWithValue.EMPTY,
                instanceContainer = instances,
            )
            throw RSocketError.Rejected("Unable to process incoming request, data is corrupted or invalid.")
        }
    }

    private fun getService(metadata: ClientMetadata): ServiceDescriptor {
        return services[metadata.serviceName] ?: throw ServiceNotFoundException(metadata.serviceName)
    }

    /**
     * Extension function to convert a value to a Payload.
     *
     * @param value The value to convert.
     * @param strategy The serialization strategy for the value.
     * @param serverMetadata The metadata to include with the server response.
     * @return The Payload representing the value.
     */
    private fun <T> T.toPayload(
        strategy: SerializationStrategy<T>,
        serverMetadata: ServerMetadata,
    ): Payload {
        return Payload(
            ByteReadPacket(protobuf.encodeToByteArray(strategy, this@toPayload)),
            ByteReadPacket(protobuf.encodeToByteArray(ServerMetadata.serializer(), serverMetadata))
        )
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun <T> Flow<T>.mapToPayload(
        serverMetadata: ServerMetadata,
        strategy: SerializationStrategy<T>,
    ): Flow<Payload> {
        return flow {
            emit(
                Payload(
                    ByteReadPacket.Empty, ByteReadPacket(protobuf.encodeToByteArray<ServerMetadata>(serverMetadata))
                )
            )
            collect {
                emit(
                    Payload(
                        data = ByteReadPacket(protobuf.encodeToByteArray(strategy, it)),
                    )
                )
            }
        }
    }
}

internal fun Payload.metadataOrFailure(): ByteArray {
    return metadata?.readBytes() ?: throw RSocketError.Invalid("Metadata with service and procedure is not specified.")
}
