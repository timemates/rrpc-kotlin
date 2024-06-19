package org.timemates.rsp.server

import io.ktor.server.routing.*
import io.ktor.utils.io.core.*
import io.rsocket.kotlin.RSocketError
import io.rsocket.kotlin.RSocketRequestHandler
import io.rsocket.kotlin.RSocketRequestHandlerBuilder
import io.rsocket.kotlin.ktor.server.rSocket
import io.rsocket.kotlin.payload.Payload
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import org.timemates.rsp.ServicesContainer
import org.timemates.rsp.annotations.ExperimentalInstancesApi
import org.timemates.rsp.annotations.ExperimentalInterceptorsApi
import org.timemates.rsp.instances.InstanceContainer
import org.timemates.rsp.instances.ProtobufInstance
import org.timemates.rsp.instances.ProvidableInstance
import org.timemates.rsp.instances.getInstance
import org.timemates.rsp.interceptors.Interceptors
import org.timemates.rsp.metadata.ClientMetadata
import org.timemates.rsp.metadata.ExtraMetadata
import org.timemates.rsp.pipeline.PipelineContext
import org.timemates.rsp.server.descriptors.ServiceDescriptor
import org.timemates.rsp.server.descriptors.procedure
import org.timemates.rsp.server.interceptors.Interceptor

/**
 * Represents a Proto server that can handle remote method calls.
 *
 * @property services The list of service descriptors for the server.
 * @property interceptors The list of interceptors for the server.
 */
public interface RSocketProtoServer : InstanceContainer, ServicesContainer {
    /**
     * Contains the list of interceptors for the RSocketProtoServer.
     *
     * Interceptors are used to intercept and modify the coroutine context and payload of remote method calls.
     * They are applied before the method is executed and can be used to perform actions such as authentication, logging,
     * or modifying the payload of the incoming request.
     *
     * Interceptors are instances of the [Interceptor] interface.
     *
     * @see Interceptor
     * @see RSocketProtoServer
     */
    @ExperimentalInterceptorsApi
    public val interceptors: Interceptors
}

/**
 * Represents a list of known procedure descriptors for the RSocketProtoServer.
 *
 * The `knownProcedures` property is a read-only property that returns a list of ProcedureDescriptor objects.
 * These represent the known procedures for the RSocketProtoServer. Each ProcedureDescriptor represents a remote
 * method call and contains information such as the name of the method, the kind of request, and the serializers
 * for the request and response objects.
 *
 * @return The list of known procedure descriptors.
 *
 * @see ProcedureDescriptor
 * @see RSocketProtoServer
 */
public val RSocketProtoServer.knownProcedures: List<ServiceDescriptor.ProcedureDescriptor<*, *>>
    get() {
        return services.fold(emptyList()) { acc, descriptor ->
            acc + descriptor.procedures
        }
    }

/**
 * Creates and configures an RSocket server with the specified endpoint and RSocketProtoServer.
 *
 * @param endpoint The endpoint to bind the server to. Default value is*/
public fun Routing.rspEndpoint(endpoint: String = "/rsp", server: RSocketProtoServer) {
    rSocket(endpoint) {
        RSocketRequestHandler {
            useServer(server)
        }
    }
}

/**
 * Creates an RSocket server endpoint on the specified routing path.
 *
 * @param endpoint The routing path for the RSocket server (default:*/
public fun Routing.rspEndpoint(endpoint: String = "/rsp", block: RSocketProtoServerBuilder.() -> Unit) {
    rspEndpoint(endpoint, RSocketProtoServerBuilder().apply(block).build())
}


@OptIn(ExperimentalSerializationApi::class, ExperimentalInstancesApi::class)
public fun RSocketRequestHandlerBuilder.useServer(server: RSocketProtoServer) {
    val services = server.services
        .associateBy { service ->
            service.name
        }
    val protobuf = server.getInstance(ProtobufInstance)!!.protoBuf

    val getClientMetadata: (Payload) -> ClientMetadata = {
        protobuf.decodeFromByteArray(it.metadataOrFailure())
    }
    val getService: (ClientMetadata) -> ServiceDescriptor = {
        services[it.serviceName] ?: throwServiceNotFound()
    }

    requestResponse { payload ->
        val metadata = getClientMetadata(payload)

        val service: ServiceDescriptor = getService(metadata)
        val method = service.procedure<ProcedureDescriptor.RequestResponse>(metadata.procedureName)
            ?: throwProcedureNotFound()

        server.runInterceptors(metadata) {
            withContext(ExtraMetadata(metadata.extra)) {
                method.execute(protobuf, payload.data)
            }
        }
    }

    requestStream { payload ->
        val metadata = getClientMetadata(payload)

        val service: ServiceDescriptor = getService(metadata)
        val method = service.procedure<ProcedureDescriptor.RequestStream>(metadata.procedureName)
            ?: throwProcedureNotFound()

        server.runInterceptors(metadata) {
            withContext(ExtraMetadata(metadata.extra)) {
                method.execute(protobuf, payload.data)
            }
        }
    }

    requestChannel { initial, payloads ->
        val metadata = getClientMetadata(initial)

        val service: ServiceDescriptor = getService(metadata)
        val method = service.procedure<ProcedureDescriptor.RequestChannel>(metadata.procedureName)
            ?: throwProcedureNotFound()

        server.runInterceptors(metadata) {
            withContext(ExtraMetadata(metadata.extra)) {
                method.execute(protobuf, initial.data, payloads.map { it.data })
            }
        }
    }
}

private fun Payload.metadataOrFailure(): ByteArray {
    return metadata?.readBytes()
        ?: throw RSocketError.Invalid("Metadata with service and procedure is not specified.")
}

private fun throwServiceNotFound(): Nothing = throw RSocketError.Invalid("Service is not found.")
private fun throwProcedureNotFound(): Nothing = throw RSocketError.Invalid("Procedure is not found.")

@OptIn(ExperimentalInstancesApi::class, ExperimentalInterceptorsApi::class)
internal class RSocketProtoServerImpl(
    override val services: List<ServiceDescriptor>,
    override val interceptors: Interceptors,
    override val instances: Map<ProvidableInstance.Key<*>, ProvidableInstance>
) : RSocketProtoServer {
    private val servicesMap = services.associateBy { it.name }

    override val key: ProvidableInstance.Key<*>
        get() = ServicesContainer.Key

    override fun service(name: String): ServiceDescriptor? {
        return servicesMap[name]
    }
}

@OptIn(ExperimentalInterceptorsApi::class, ExperimentalInstancesApi::class)
private suspend inline fun <T : Any, R : Any> RSocketProtoServer.runInterceptors(
    data: T,
    clientMetadata: ClientMetadata,
    procedureDescriptor: ServiceDescriptor.ProcedureDescriptor<T, R>,
    crossinline block: suspend () -> R
): R {
    var coroutineContext = currentCoroutineContext()

    if (interceptors.forRequests.isNotEmpty()) {
        var state: PipelineContext<*, *> = PipelineContext(
            services,
            clientMetadata,
            data,
            instances,
        )

        interceptors.forRequests.forEach { inteceptor ->
            with(inteceptor) {
                coroutineContext = inteceptor.interceptRequest(
                    coroutineContext, clientMetadata
                )
            }
        }
    }

    return withContext(coroutineContext) {
        block()
    }
}