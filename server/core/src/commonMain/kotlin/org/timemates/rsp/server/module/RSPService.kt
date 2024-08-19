package org.timemates.rsp.server.module

import org.timemates.rsp.annotations.InternalRSProtoAPI
import org.timemates.rsp.instances.CoroutineContextInstanceContainer
import org.timemates.rsp.instances.InstanceContainer
import org.timemates.rsp.server.module.descriptors.ServiceDescriptor
import kotlin.coroutines.coroutineContext

/**
 * Annotation-marker for the services that are generated.
 */
public abstract class RSPService {
    /**
     * Represents a descriptor for a service. Contains name, procedures and so on.
     *
     * @see ProcedureDescriptor
     */
    public abstract val descriptor: ServiceDescriptor
}