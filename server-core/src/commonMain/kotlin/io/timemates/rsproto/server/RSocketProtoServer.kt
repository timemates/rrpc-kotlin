package io.timemates.rsproto.server

import io.ktor.server.routing.*
import io.ktor.utils.io.core.*
import io.rsocket.kotlin.RSocketError
import io.rsocket.kotlin.RSocketRequestHandler
import io.rsocket.kotlin.RSocketRequestHandlerBuilder
import io.rsocket.kotlin.ktor.server.rSocket
import io.rsocket.kotlin.payload.Payload
import io.timemates.rsproto.metadata.ExtraMetadata
import io.timemates.rsproto.metadata.Metadata
import io.timemates.rsproto.server.annotations.ExperimentalInstancesApi
import io.timemates.rsproto.server.annotations.ExperimentalInterceptorsApi
import io.timemates.rsproto.server.descriptors.ProcedureDescriptor
import io.timemates.rsproto.server.descriptors.ServiceDescriptor
import io.timemates.rsproto.server.descriptors.procedure
import io.timemates.rsproto.server.instances.InstanceContainer
import io.timemates.rsproto.server.instances.ProtobufInstance
import io.timemates.rsproto.server.instances.ProvidableInstance
import io.timemates.rsproto.server.instances.getInstance
import io.timemates.rsproto.server.interceptors.Interceptor
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray

/**
 * Represents a Proto server that can handle remote method calls.
 *
 * @property services The list of service descriptors for the server.
 * @property interceptors The list of interceptors for the server.
 */
public interface RSocketProtoServer : InstanceContainer {
    /**
     * Represents a list of service descriptors for remote services.
     *
     * A `ServiceDescriptor` represents a service descriptor for a remote service. It contains the name of the service and a list of procedure descriptors for the service.
     *
     * @since 1.0
     */
    public val services: List<ServiceDescriptor>

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
    @OptIn(ExperimentalInterceptorsApi::class)
    public val interceptors: List<Interceptor>
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
public val RSocketProtoServer.knownProcedures: List<ProcedureDescriptor>
    get() {
        return services.fold(emptyList()) { acc, descriptor ->
            acc + descriptor.procedures
        }
    }

/**
 * Creates and configures an RSocket server with the specified endpoint and RSocketProtoServer.
 *
 * @param endpoint The endpoint to bind the server to. Default value is*/
public fun Routing.rSocketServer(endpoint: String = "/rsocket", server: RSocketProtoServer) {
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
public fun Routing.rSocketServer(endpoint: String = "/rsocket", block: RSocketProtoServerBuilder.() -> Unit) {
    rSocketServer(endpoint, RSocketProtoServerBuilder().apply(block).build())
}


@OptIn(ExperimentalSerializationApi::class, ExperimentalInstancesApi::class)
public fun RSocketRequestHandlerBuilder.useServer(server: RSocketProtoServer) {
    val services = server.services
        .associateBy { service ->
            service.name
        }
    val protobuf = server.getInstance(ProtobufInstance)!!.protoBuf

    val getMetadata: (Payload) -> Metadata = {
        protobuf.decodeFromByteArray(it.metadataOrFailure())
    }
    val getService: (Metadata) -> ServiceDescriptor = {
        services[it.serviceName] ?: throwServiceNotFound()
    }

    requestResponse { payload ->
        val metadata = getMetadata(payload)

        val service: ServiceDescriptor = getService(metadata)
        val method = service.procedure<ProcedureDescriptor.RequestResponse>(metadata.procedureName)
            ?: throwProcedureNotFound()
        withContext(ExtraMetadata(metadata.extra)) {
            method.execute(protobuf, payload.data)
        }
    }

    requestStream { payload ->
        val metadata = getMetadata(payload)

        val service: ServiceDescriptor = getService(metadata)
        val method = service.procedure<ProcedureDescriptor.RequestStream>(metadata.procedureName)
            ?: throwProcedureNotFound()

        withContext(ExtraMetadata(metadata.extra)) {
            method.execute(protobuf, payload.data)
        }
    }

    requestChannel { initial, payloads ->
        val metadata = getMetadata(initial)

        val service: ServiceDescriptor = getService(metadata)
        val method = service.procedure<ProcedureDescriptor.RequestChannel>(metadata.procedureName)
            ?: throwProcedureNotFound()

        withContext(ExtraMetadata(metadata.extra)) {
            method.execute(protobuf, initial.data, payloads.map { it.data })
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
    override val interceptors: List<Interceptor>,
    override val instances: Map<ProvidableInstance.Key<*>, ProvidableInstance>
) : RSocketProtoServer