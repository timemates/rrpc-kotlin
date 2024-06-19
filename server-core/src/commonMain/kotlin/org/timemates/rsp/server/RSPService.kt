package org.timemates.rsp.server

import org.timemates.rsp.annotations.ExperimentalInstancesApi
import org.timemates.rsp.instances.CoroutineContextInstanceContainer
import org.timemates.rsp.instances.InstanceContainer
import org.timemates.rsp.instances.ProvidableInstance
import org.timemates.rsp.instances.getInstance
import org.timemates.rsp.metadata.ExtraMetadata
import org.timemates.rsp.server.descriptors.ServiceDescriptor
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


    @ExperimentalInstancesApi
    protected suspend fun instances(): InstanceContainer {
        return coroutineContext[CoroutineContextInstanceContainer]?.container
            ?: error("Call from the wrong context.")
    }
}