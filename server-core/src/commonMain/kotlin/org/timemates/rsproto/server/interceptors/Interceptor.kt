package org.timemates.rsproto.server.interceptors

import org.timemates.rsproto.metadata.Metadata
import org.timemates.rsproto.server.annotations.ExperimentalInterceptorsApi
import org.timemates.rsproto.server.instances.InstanceContainer
import kotlin.coroutines.CoroutineContext

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