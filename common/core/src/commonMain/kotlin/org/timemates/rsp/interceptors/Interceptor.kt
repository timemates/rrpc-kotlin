@file:OptIn(InternalRSProtoAPI::class)

package org.timemates.rsp.interceptors

import org.timemates.rsp.DataVariant
import org.timemates.rsp.Failure
import org.timemates.rsp.annotations.ExperimentalInterceptorsApi
import org.timemates.rsp.annotations.InternalRSProtoAPI
import org.timemates.rsp.instances.InstanceContainer
import org.timemates.rsp.metadata.ClientMetadata
import org.timemates.rsp.metadata.RSPMetadata
import org.timemates.rsp.metadata.ServerMetadata
import org.timemates.rsp.options.Options

/**
 * Represents an interceptor that processes metadata of type [TMetadata].
 *
 * @param TMetadata The type of metadata that the interceptor processes.
 */
@ExperimentalInterceptorsApi
public interface Interceptor<TMetadata : RSPMetadata> {
    /**
     * Intercepts and processes the given [context].
     *
     * **ANY consumption of request input / output that is Flow should rely
     * on the functions like [kotlinx.coroutines.flow.onEach]. Otherwise, it will lead to data loss or performance problems.**
     *
     * @param context The context containing metadata to be processed by the interceptor.
     * @return The processed [InterceptorContext] with potentially modified metadata.
     */
    public suspend fun intercept(
        context: InterceptorContext<TMetadata>,
    ): InterceptorContext<TMetadata>
}

/**
 * A type alias for an interceptor that processes client metadata.
 */
@ExperimentalInterceptorsApi
public typealias RequestInterceptor = Interceptor<ClientMetadata>

/**
 * A type alias for an interceptor that processes server metadata.
 */
@ExperimentalInterceptorsApi
public typealias ResponseInterceptor = Interceptor<ServerMetadata>

@ExperimentalInterceptorsApi
public data class Interceptors(
    val request: List<Interceptor<ClientMetadata>>,
    val response: List<Interceptor<ServerMetadata>>,
) {
    /**
     * Runs input interceptors and returns the result of the provided block.
     *
     * @param data The input data.
     * @param clientMetadata The client metadata.
     * @param options The options for the procedure.
     * @param instanceContainer The instance container.
     * @param block The block of code to execute after running the interceptors.
     * @return The result of the block execution.
     */
    @InternalRSProtoAPI
    public suspend inline fun runInputInterceptors(
        data: DataVariant<*>,
        clientMetadata: ClientMetadata,
        options: Options,
        instanceContainer: InstanceContainer,
    ): InterceptorContext<ClientMetadata>? {
        if (request.isNotEmpty()) {
            val initialContext = InterceptorContext(data, clientMetadata, options, instanceContainer)
            return request.fold(initialContext) { acc, interceptor ->
                try {
                    interceptor.intercept(acc)
                } catch (e: Exception) {
                    interceptor.intercept(acc.copy(data = Failure(e)))
                }
            }
        }
        
        return null
    }

    /**
     * Runs output interceptors on the provided data.
     *
     * **API Note**: The API is considered internal, to backward-compatibility is
     * guaranteed.
     *
     * @param data The output data.
     * @param serverMetadata The server metadata.
     * @param options The options for the procedure.
     * @param instanceContainer The instance container.
     *
     * @return [InterceptorContext] or null if no interceptors were involved.
     */
    @InternalRSProtoAPI
    public suspend fun runOutputInterceptors(
        data: DataVariant<*>,
        serverMetadata: ServerMetadata,
        options: Options,
        instanceContainer: InstanceContainer
    ): InterceptorContext<ServerMetadata>? {
        return if (response.isNotEmpty()) {
            val initialContext = InterceptorContext(data, serverMetadata, options, instanceContainer)
            response.fold(initialContext) { acc, interceptor ->
                try {
                    interceptor.intercept(acc)
                } catch (e: Exception) {
                    interceptor.intercept(acc.copy(data = Failure(e)))
                }
            }
        } else null
    }
}