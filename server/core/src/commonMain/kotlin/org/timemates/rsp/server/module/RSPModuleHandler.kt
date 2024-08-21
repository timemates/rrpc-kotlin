@file:OptIn(ExperimentalSerializationApi::class, ExperimentalInterceptorsApi::class, InternalRSProtoAPI::class)

package org.timemates.rsp.server.module

import io.ktor.utils.io.core.*
import io.rsocket.kotlin.RSocketError
import io.rsocket.kotlin.RSocketRequestHandlerBuilder
import io.rsocket.kotlin.payload.Payload
import kotlinx.coroutines.flow.*
import kotlinx.serialization.*
import org.timemates.rsp.*
import org.timemates.rsp.annotations.ExperimentalInterceptorsApi
import org.timemates.rsp.annotations.InternalRSProtoAPI
import org.timemates.rsp.exceptions.ProcedureNotFoundException
import org.timemates.rsp.exceptions.ServiceNotFoundException
import org.timemates.rsp.instances.ProtobufInstance
import org.timemates.rsp.interceptors.InterceptorContext
import org.timemates.rsp.metadata.ClientMetadata
import org.timemates.rsp.metadata.ServerMetadata
import org.timemates.rsp.options.Options
import org.timemates.rsp.server.RequestContext
import org.timemates.rsp.server.module.descriptors.ProcedureDescriptor
import org.timemates.rsp.server.module.descriptors.ServiceDescriptor
import org.timemates.rsp.server.module.descriptors.procedure
import org.timemates.rsp.server.toRequestContext

/**
 * Handler class for setting up RSocket request handlers.
 *
 * @param module The RSPModule instance to handle requests.
 */
@Suppress("DuplicatedCode")
public class RSPModuleHandler(private val module: RSPModule) {
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
        }
    }

    private fun RSocketRequestHandlerBuilder.requestResponseHandler() {
        requestResponse { payload ->
            val metadata = getClientMetadata(payload)
            val service = getService(metadata)
            val method = service.procedure<ProcedureDescriptor.RequestResponse<Any, Any>>(metadata.procedureName)
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
                module,
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
                instanceContainer = startContext?.instances ?: module,
            )

            if (finalContext?.data is Failure) throw (finalContext.data as Failure).exception

            ((finalContext?.data as? Single)?.value ?: result).toPayload(
                strategy = method.outputSerializer, serverMetadata = serverMetadata
            )
        }
    }

    private fun RSocketRequestHandlerBuilder.requestStreamHandler() {
        requestStream { payload ->
            val metadata = getClientMetadata(payload)
            val service = getService(metadata)
            val method = service.procedure<ProcedureDescriptor.RequestStream<Any, Any>>(metadata.procedureName)
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
                module,
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
                instanceContainer = startContext?.instances ?: module,
            )

            if (finalContext?.data is Failure) throw (finalContext.data as Failure).exception

            ((finalContext?.data as? Streaming)?.flow ?: result).mapToPayload(
                serverMetadata = serverMetadata, strategy = method.outputSerializer
            )
        }
    }


    private fun RSocketRequestHandlerBuilder.requestChannelHandler() {
        requestChannel { initial, payloads ->
            val metadata = getClientMetadata(initial)
            val service = getService(metadata)
            val method = service.procedure<ProcedureDescriptor.RequestChannel<Any, Any>>(metadata.procedureName)
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
                    module,
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
                instanceContainer = startContext?.instances ?: module,
            )

            if (finalContext?.data is Failure) throw (finalContext.data as Failure).exception

            ((finalContext?.data as? Streaming)?.flow ?: result).mapToPayload(
                serverMetadata = serverMetadata, strategy = method.outputSerializer
            )
        }
    }


    @OptIn(InternalRSProtoAPI::class)
    private suspend fun handleException(
        exception: Exception,
        method: ProcedureDescriptor<*, *>?,
        prevContext: InterceptorContext<ClientMetadata>? = null,
    ): Nothing {
        val resultException = try {
            module.interceptors.runOutputInterceptors(
                data = Failure(exception),
                serverMetadata = serverMetadata,
                options = prevContext?.options ?: method?.options ?: Options.EMPTY,
                instanceContainer = prevContext?.instances ?: module,
            )?.data?.requireFailure() ?: exception
        } catch (e: Exception) {
            // todo special logging
            e
        }

        throw resultException
    }

    private fun getClientMetadata(payload: Payload): ClientMetadata {
        return try {
            protobuf.decodeFromByteArray(payload.metadataOrFailure())
        } catch (e: Exception) {
            // TODO logging the exception
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
