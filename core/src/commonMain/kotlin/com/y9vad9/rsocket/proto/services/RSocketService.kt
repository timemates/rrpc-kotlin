package com.y9vad9.rsocket.proto.services

import com.y9vad9.rsocket.proto.services.ServiceDescriptor

import com.y9vad9.rsocket.proto.requests.ProcedureDescriptor

/**
 * Annotation-marker for the services that are generated.
 */
public interface RSocketService {
    /**
     * Represents a descriptor for a service. Contains name, procedures and so on.
     *
     * @see ProcedureDescriptor
     */
    public val descriptor: ServiceDescriptor
}