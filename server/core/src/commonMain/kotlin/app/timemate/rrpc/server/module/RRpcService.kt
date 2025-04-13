package app.timemate.rrpc.server.module

import app.timemate.rrpc.server.module.descriptors.ServiceDescriptor

/**
 * Annotation-marker for the services that are generated.
 */
public interface RRpcService {
    /**
     * Represents a descriptor for a service. Contains name, procedures and so on.
     */
    public val descriptor: ServiceDescriptor
}