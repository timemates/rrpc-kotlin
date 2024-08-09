@file:OptIn(ExperimentalSerializationApi::class, ExperimentalInterceptorsApi::class, InternalRSProtoAPI::class)

package org.timemates.rsp.server.module

import io.ktor.utils.io.core.*
import io.rsocket.kotlin.RSocketError
import io.rsocket.kotlin.RSocketRequestHandlerBuilder
import io.rsocket.kotlin.payload.Payload
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import kotlinx.serialization.*
import org.timemates.rsp.DataVariant
import org.timemates.rsp.annotations.ExperimentalInterceptorsApi
import org.timemates.rsp.annotations.InternalRSProtoAPI
import org.timemates.rsp.instances.CoroutineContextInstanceContainer
import org.timemates.rsp.instances.ProtobufInstance
import org.timemates.rsp.metadata.ClientMetadata
import org.timemates.rsp.metadata.ServerMetadata
import org.timemates.rsp.requireStreaming
import org.timemates.rsp.server.RequestContext
import org.timemates.rsp.server.module.descriptors.ServiceDescriptor
import org.timemates.rsp.server.module.descriptors.ServiceDescriptor.ProcedureDescriptor
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
    private val serverMetadata = ServerMetadata()

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
                ?: throwProcedureNotFound()
            val data =
                DataVariant.Single(protobuf.decodeFromByteArray(method.inputSerializer, payload.data.readBytes()))

            val startContext = module.interceptors.runInputInterceptors(
                data = data,
                clientMetadata = metadata,
                options = method.options,
                module,
            )

            val result = method.execute(
                context = startContext?.toRequestContext() ?: RequestContext(module, metadata, method.options),
                input = ((startContext?.data ?: data) as DataVariant.Single).value,
            )

            return@requestResponse module.interceptors.runOutputInterceptors(
                data = DataVariant.Single(result),
                serverMetadata = serverMetadata,
                options = method.options,
                instanceContainer = startContext?.instances ?: module,
            ).let {
                ((it?.data as? DataVariant.Single)?.value ?: result).toPayload(
                    strategy = method.outputSerializer,
                    serverMetadata = it?.metadata ?: serverMetadata
                )
            }
        }
    }

    private fun RSocketRequestHandlerBuilder.requestStreamHandler() {
        requestStream { payload ->
            val metadata = getClientMetadata(payload)

            val service: ServiceDescriptor = getService(metadata)
            val method = service.procedure<ProcedureDescriptor.RequestStream<Any, Any>>(metadata.procedureName)
                ?: throwProcedureNotFound()

            val data = DataVariant.Single(
                protobuf.decodeFromByteArray(method.inputSerializer, payload.data.readBytes())
            )

            val startContext = module.interceptors.runInputInterceptors(
                data = data,
                clientMetadata = metadata,
                options = method.options,
                module,
            )

            val result = method.execute(
                context = startContext?.toRequestContext() ?: RequestContext(module, metadata, method.options),
                value = ((startContext?.data ?: data) as DataVariant.Single).value,
            )

            return@requestStream module.interceptors.runOutputInterceptors(
                data = DataVariant.Streaming(result),
                serverMetadata = serverMetadata,
                options = startContext?.options ?: method.options,
                instanceContainer = startContext?.instances ?: module,
            ).let { interceptorContext ->
                ((interceptorContext?.data as? DataVariant.Streaming)?.flow ?: result)
                    .mapToPayload(serverMetadata, method.outputSerializer)
            }
        }
    }

    private fun RSocketRequestHandlerBuilder.requestChannelHandler() {
        requestChannel { initial, payloads ->
            val metadata = getClientMetadata(initial)

            val service: ServiceDescriptor = getService(metadata)
            val method = service.procedure<ProcedureDescriptor.RequestChannel<Any, Any>>(metadata.procedureName)
                ?: throwProcedureNotFound()

            val data = DataVariant.Streaming(
                payloads.map {
                    protobuf.decodeFromByteArray(method.inputSerializer, it.data.readBytes())
                }
            )

            val startContext = module.interceptors.runInputInterceptors(
                data = data,
                clientMetadata = metadata,
                options = method.options,
                module,
            )

            val result = method.execute(
                context = startContext?.toRequestContext() ?: RequestContext(module, metadata, method.options),
                flow = ((startContext?.data ?: data) as DataVariant.Streaming).flow,
            )

            return@requestChannel module.interceptors.runOutputInterceptors(
                data = DataVariant.Streaming(result),
                serverMetadata = serverMetadata,
                options = method.options,
                instanceContainer = startContext?.instances ?: module,
            ).let { interceptorContext ->
                ((interceptorContext?.data as? DataVariant.Streaming)?.flow ?: result)
                    .mapToPayload(serverMetadata, method.outputSerializer)
            }
        }
    }

    private fun getClientMetadata(payload: Payload): ClientMetadata {
        return protobuf.decodeFromByteArray(payload.metadataOrFailure())
    }

    private fun getService(metadata: ClientMetadata): ServiceDescriptor {
        return services[metadata.serviceName] ?: throwServiceNotFound()
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
            emit(Payload(ByteReadPacket.Empty, ByteReadPacket(protobuf.encodeToByteArray<ServerMetadata>(serverMetadata))))
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

internal fun throwServiceNotFound(): Nothing = throw RSocketError.Invalid("Service is not found.")
internal fun throwProcedureNotFound(): Nothing = throw RSocketError.Invalid("Procedure is not found.")