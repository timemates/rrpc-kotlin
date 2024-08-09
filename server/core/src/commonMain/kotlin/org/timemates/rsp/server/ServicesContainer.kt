package org.timemates.rsp.server

import org.timemates.rsp.instances.ProvidableInstance
import org.timemates.rsp.server.module.descriptors.ServiceDescriptor

public interface ServicesContainer : ProvidableInstance {
    public companion object Key : ProvidableInstance.Key<ServicesContainer>
    /**
     * Represents a list of service descriptors for remote services.
     *
     * A `ServiceDescriptor` represents a service descriptor for a remote service.
     * It contains the name of the service and a list of procedure descriptors for the service.
     *
     * @since 1.0
     */
    public val services: List<ServiceDescriptor>

    /**
     * Gets service by [name].
     */
    public fun service(name: String): ServiceDescriptor?
}