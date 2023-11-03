package com.y9vad9.rsocket.proto

import com.y9vad9.rsocket.proto.annotations.ExperimentalInterceptorsApi
import com.y9vad9.rsocket.proto.interceptors.Interceptor
import com.y9vad9.rsocket.proto.requests.ProcedureDescriptor
import com.y9vad9.rsocket.proto.services.ServiceDescriptor

/**
 * Represents a Proto server that can handle remote method calls.
 *
 * @property services The list of service descriptors for the server.
 * @property interceptors The list of interceptors for the server.
 */
public interface RSocketProtoServer {
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

    /**
     * Starts the Proto server.
     *
     * This method is a suspend function that starts the Proto server. It should be called to start the server and
     * begin handling remote method calls from clients.
     */
    public suspend fun start()
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
public val RSocketProtoServer.knownProcedures: List<ProcedureDescriptor<*, *>>
    get() {
        return services.fold(listOf()) { acc, descriptor ->
            acc + descriptor.procedures
        }
    }