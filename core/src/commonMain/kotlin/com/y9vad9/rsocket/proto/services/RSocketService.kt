package com.y9vad9.rsocket.proto.services

import com.y9vad9.rsocket.proto.annotations.ExperimentalInstancesApi
import com.y9vad9.rsocket.proto.providable.CoroutineContextInstanceContainer
import com.y9vad9.rsocket.proto.providable.ProvidableInstance
import com.y9vad9.rsocket.proto.providable.getInstance

import com.y9vad9.rsocket.proto.procedures.ProcedureDescriptor
import kotlin.coroutines.coroutineContext

/**
 * Annotation-marker for the services that are generated.
 */
public abstract class RSocketService {
    /**
     * Represents a descriptor for a service. Contains name, procedures and so on.
     *
     * @see ProcedureDescriptor
     */
    public abstract val descriptor: ServiceDescriptor


    /**
     * Retrieves an instance of the specified type based on the provided key.
     *
     * **Note**: Works only within service procedures as instances are provided via
     * [kotlin.coroutines.CoroutineContext].
     *
     * @param key The key representing the type of instance to retrieve.
     * @return The instance of type T, or null if it doesn't exist.
     */
    @ExperimentalInstancesApi
    protected suspend fun <T : ProvidableInstance> getInstance(key: ProvidableInstance.Key<T>): T? {
        return coroutineContext[CoroutineContextInstanceContainer]?.container?.getInstance(key)
    }
}