package org.timemates.rsp.interceptors

import org.timemates.rsp.annotations.ExperimentalInterceptorsApi
import org.timemates.rsp.pipeline.PipelineContext

/**
 * An experimental API for intercepting requests and responses in a pipeline.
 * This API is subject to change or removal in future versions.
 */
@ExperimentalInterceptorsApi
public interface Interceptor {
    /**
     * Represents an interceptor for processing requests in a pipeline.
     */
    public interface Request : Interceptor {
        /**
         * Intercepts the request in the pipeline. In meaning of server-side,
         * it intercepts incoming request, before it reaches implemented RPC Service. For client-side,
         * it executes thing before request is sent to the server-side.
         *
         * @param state The current state of the pipeline.
         * @return The modified state after request interception.
         */
        public fun interceptRequest(
            state: PipelineContext<*>
        ): PipelineContext<*>
    }

    /**
     * Represents an interceptor for processing responses in a pipeline.
     */
    public interface Response : Interceptor {
        /**
         * Intercepts the response in the pipeline. In meaning of server-side, it intercepts
         * outgoing response, before it reaches the client-side.
         * For client-side, it executes thing after request is accepted, but before it's used.
         *
         * @param state The current state of the pipeline.
         * @return The modified state after response interception.
         */
        public fun interceptResponse(
            state: PipelineContext<*>
        ): PipelineContext<*>
    }
}