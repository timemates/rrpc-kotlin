@file:OptIn(ExperimentalSerializationApi::class, ExperimentalInterceptorsApi::class, InternalRRpcAPI::class)

package app.timemate.rrpc.server.module

import io.ktor.utils.io.core.*
import io.rsocket.kotlin.RSocketError
import io.rsocket.kotlin.RSocketRequestHandlerBuilder
import io.rsocket.kotlin.payload.Payload
import io.rsocket.kotlin.payload.buildPayload
import io.rsocket.kotlin.payload.data
import io.rsocket.kotlin.payload.metadata
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import app.timemate.rrpc.*
import app.timemate.rrpc.annotations.ExperimentalInterceptorsApi
import app.timemate.rrpc.annotations.InternalRRpcAPI
import app.timemate.rrpc.exceptions.ProcedureNotFoundException
import app.timemate.rrpc.exceptions.ServiceNotFoundException
import app.timemate.rrpc.instances.InstanceContainer
import app.timemate.rrpc.instances.ProtobufInstance
import app.timemate.rrpc.interceptors.InterceptorContext
import app.timemate.rrpc.interceptors.Interceptors
import app.timemate.rrpc.metadata.ClientMetadata
import app.timemate.rrpc.metadata.ServerMetadata
import app.timemate.rrpc.options.OptionsWithValue
import app.timemate.rrpc.server.RequestContext
import app.timemate.rrpc.server.module.descriptors.ProcedureDescriptor
import app.timemate.rrpc.server.module.descriptors.ServiceDescriptor
import app.timemate.rrpc.server.module.descriptors.procedure
import app.timemate.rrpc.server.toRequestContext

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
                service.procedure<ProcedureDescriptor.RequestResponse<RSProtoType, RSProtoType>>(metadata.procedureName)
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
                service.procedure<ProcedureDescriptor.RequestStream<RSProtoType, RSProtoType>>(metadata.procedureName)
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
                service.procedure<ProcedureDescriptor.RequestChannel<RSProtoType, RSProtoType>>(metadata.procedureName)
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
        val method = service.procedure<ProcedureDescriptor.FireAndForget<RSProtoType>>(metadata.procedureName)
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
     * @receiver The value to convert.
     * @param strategy The serialization strategy for the value.
     * @param serverMetadata The metadata to include with the server response.
     * @return The Payload representing the value.
     */
    private fun <T> T.toPayload(
        strategy: SerializationStrategy<T>,
        serverMetadata: ServerMetadata,
    ): Payload {
        return buildPayload {
            data(protobuf.encodeToByteArray(strategy, this@toPayload))
            metadata(protobuf.encodeToByteArray(ServerMetadata.serializer(), serverMetadata))
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun <T> Flow<T>.mapToPayload(
        serverMetadata: ServerMetadata,
        strategy: SerializationStrategy<T>,
    ): Flow<Payload> {
        return flow {
            emit(
                buildPayload {
                    metadata(protobuf.encodeToByteArray<ServerMetadata>(serverMetadata))
                    data(byteArrayOf())
                }
            )
            collect {
                emit(
                    buildPayload {
                        data(protobuf.encodeToByteArray(strategy, it))
                    }
                )
            }
        }
    }
}

internal fun Payload.metadataOrFailure(): ByteArray {
    return metadata?.readBytes() ?: throw RSocketError.Invalid("Metadata with service and procedure is not specified.")
}
