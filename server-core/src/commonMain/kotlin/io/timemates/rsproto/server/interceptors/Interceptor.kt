package io.timemates.rsproto.server.interceptors

import io.rsocket.kotlin.payload.Payload
import io.timemates.rsproto.metadata.ExtraMetadata
import io.timemates.rsproto.server.annotations.ExperimentalInstancesApi
import io.timemates.rsproto.server.annotations.ExperimentalInterceptorsApi
import io.timemates.rsproto.server.instances.CoroutineContextInstanceContainer
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
    public abstract fun intercept(
        coroutineContext: CoroutineContext,
        incoming: Payload,
    ): CoroutineContext

    /**
     * Retrieves an instance of the specified type based on the provided key.
     *
     * **Note**: Works only within service procedures as instances are provided via
     * [kotlin.coroutines.CoroutineContext].
     *
     * TODO: Move and make it as extension when context-receivers will be stable.
     *
     * @param key The key representing the type of instance to retrieve.
     * @return The instance of type T, or null if it doesn't exist.
     */
    @ExperimentalInstancesApi
    protected suspend fun <T : ProvidableInstance> getInstance(key: ProvidableInstance.Key<T>): T? {
        return coroutineContext[CoroutineContextInstanceContainer]?.container?.getInstance(key)
    }

    /**
     * Retrieves the extra metadata associated with the coroutine context (current request).
     *
     * TODO: Move and make it as extension when context-receivers will be stable.
     *
     * @return The extra metadata as a map where the keys are strings and the values are byte arrays.
     */
    protected suspend fun getExtras(): Map<String, ByteArray> =
        coroutineContext[ExtraMetadata]?.extra ?: emptyMap()
}