package org.timemates.rsp.server.interceptors

import org.timemates.rsp.server.annotations.ExperimentalInterceptorsApi
import org.timemates.rsp.pipeline.PipelineContext
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

     * @return The modified coroutine context.
     */
    public abstract fun intercept(
        state: PipelineContext<*>
    ): CoroutineContext
}