package com.y9vad9.rsocket.proto.services

import com.y9vad9.rsocket.proto.requests.ProcedureDescriptor

/**
 * Represents a service descriptor for a remote service.
 *
 * @property name The name of the service.
 * @property procedures The list of procedure descriptors for the service.
 */
public data class ServiceDescriptor(
    public val name: String,
    public val procedures: List<ProcedureDescriptor<*, *>>,
)