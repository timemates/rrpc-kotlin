package com.y9vad9.rsocket.proto.services

import com.y9vad9.rsocket.proto.procedures.ProcedureDescriptor
import com.y9vad9.rsocket.proto.procedures.RequestKind

/**
 * Represents a service descriptor for a remote service.
 *
 * @property name The name of the service.
 * @property procedures The list of procedure descriptors for the service.
 */
public data class ServiceDescriptor(
    public val name: String,
    public val procedures: List<ProcedureDescriptor>,
) {
    private val proceduresMap = procedures.associateBy {
        it.name to when(it) {
            is ProcedureDescriptor.RequestResponse -> RequestKind.REQUEST_RESPONSE
            is ProcedureDescriptor.RequestStream -> RequestKind.REQUEST_STREAM
            is ProcedureDescriptor.RequestChannel -> RequestKind.REQUEST_CHANNEL
        }
    }

    public fun procedure(
        name: String,
        kind: RequestKind
    ): ProcedureDescriptor? {
        return proceduresMap[name to kind]
    }
}