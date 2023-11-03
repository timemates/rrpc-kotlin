package com.y9vad9.rsocket.proto.interceptors

import com.y9vad9.rsocket.proto.annotations.ExperimentalInterceptorsApi
import io.rsocket.kotlin.payload.Payload
import kotlin.coroutines.CoroutineContext

/**
 * Represents an interceptor that can be used to intercept and modify the coroutine
 * context and payload.
 *
 * @since 1.0
 */
@ExperimentalInterceptorsApi
public interface Interceptor {
    /**
     * Calls the `intercept` method of an interceptor to intercept and modify the coroutine context and payload.
     *
     * @param coroutineContext The current coroutine context.
     * @param incoming The payload of the incoming request.
     * @return The modified coroutine context.
     */
    public fun intercept(
        coroutineContext: CoroutineContext,
        incoming: Payload,
    ): CoroutineContext
}