package io.timemates.rsproto.server.interceptors

import io.rsocket.kotlin.payload.Payload
import io.timemates.rsproto.metadata.ExtraMetadata
import io.timemates.rsproto.metadata.Metadata
import io.timemates.rsproto.server.annotations.ExperimentalInstancesApi
import io.timemates.rsproto.server.annotations.ExperimentalInterceptorsApi
import io.timemates.rsproto.server.instances.CoroutineContextInstanceContainer
import io.timemates.rsproto.server.instances.InstanceContainer
import io.timemates.rsproto.server.instances.ProvidableInstance
import io.timemates.rsproto.server.instances.getInstance
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

/**
 * Represents an interceptor that can be used to intercept and modify the coroutine
 * context and payload.
 *
 * @since 1.0
 */
@ExperimentalInterceptorsApi
public abstract class Interceptor {
    /**
     * Calls the `intercept` method of an interceptor to intercept and modify the coroutine context and payload.
     *
     * @param coroutineContext The current coroutine context.
     * @param incoming The payload of the incoming request.
     * @return The modified coroutine context.
     */
    public abstract fun InterceptorScope.intercept(
        coroutineContext: CoroutineContext,
        metadata: Metadata,
    ): CoroutineContext
}

public data class InterceptorScope(
    public val instances: InstanceContainer,
)