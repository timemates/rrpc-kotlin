package app.timemate.rrpc.server

import app.timemate.rrpc.instances.ProvidableInstance
import app.timemate.rrpc.interceptors.InterceptorContext
import app.timemate.rrpc.metadata.ClientMetadata
import app.timemate.rrpc.server.module.descriptors.ServiceDescriptor

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

/**
 * Resolves service by incoming metadata and instance container.
 */
public val InterceptorContext<ClientMetadata>.service: ServiceDescriptor
    get() = instances.getInstance(ServicesContainer)?.service(metadata.serviceName)
        ?: error(
            "Unable to resolve the service: InstanceContainer is not including needed ServiceContainer or it's different" +
                " instance without origin service. The only reason why it may happen: some of the interceptors " +
                "replaced it with a new InstanceContainer without taking into account ServiceContainer. Or maybe you called it from client side?"
        )