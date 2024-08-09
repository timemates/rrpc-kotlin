@file:OptIn(ExperimentalInterceptorsApi::class)

package org.timemates.rsp.client.config

import io.rsocket.kotlin.RSocket
import kotlinx.serialization.ExperimentalSerializationApi
import org.timemates.rsp.annotations.ExperimentalInterceptorsApi
import org.timemates.rsp.annotations.InternalRSProtoAPI
import org.timemates.rsp.instances.*
import org.timemates.rsp.interceptors.Interceptor
import org.timemates.rsp.interceptors.Interceptors
import org.timemates.rsp.metadata.ClientMetadata
import org.timemates.rsp.metadata.ServerMetadata
import kotlin.properties.Delegates

public data class RSPClientConfig @OptIn(ExperimentalInterceptorsApi::class) constructor(
    public val rsocket: RSocket,
    public val interceptors: Interceptors,
    public val instances: InstanceContainer,
) {
    public companion object {
        public fun create(block: Builder.() -> Unit): RSPClientConfig {
            return Builder().apply {
                @OptIn(ExperimentalSerializationApi::class)
                // By default, Protobuf should always be present
                instances { protobuf() }
                block()
            }.build()
        }

        public fun builder(): Builder = Builder().apply {
            @OptIn(ExperimentalSerializationApi::class)
            // By default, Protobuf should always be present
            instances { protobuf() }
        }
    }

    public class Builder {
        /** List of interceptors for request processing */
        @ExperimentalInterceptorsApi
        private val requestInterceptors: MutableList<Interceptor<ClientMetadata>> = mutableListOf()

        /** List of interceptors for response processing */
        @ExperimentalInterceptorsApi
        private val responseInterceptors: MutableList<Interceptor<ServerMetadata>> = mutableListOf()

        /** The RSocket instance to use */
        private var rsocket: RSocket by Delegates.notNull()

        /** The container of provided instances */
        private var instancesContainer: InstanceContainer? = null

        /**
         * Adds a list of interceptors for processing requests.
         * @param interceptors List of request interceptors to add.
         * @return This builder instance.
         */
        @ExperimentalInterceptorsApi
        public fun requestInterceptors(interceptors: List<Interceptor<ClientMetadata>>): Builder = apply {
            this.requestInterceptors += interceptors
        }

        /**
         * Adds one or more interceptors for processing requests.
         * @param interceptors One or more request interceptors to add.
         * @return This builder instance.
         */
        @ExperimentalInterceptorsApi
        public fun requestInterceptors(vararg interceptors: Interceptor<ClientMetadata>): Builder = apply {
            this.requestInterceptors += interceptors
        }

        /**
         * Adds an interceptor for processing requests.
         * @param interceptor The request interceptor to add.
         * @return This builder instance.
         */
        @ExperimentalInterceptorsApi
        public fun requestInterceptor(interceptor: Interceptor<ClientMetadata>): Builder = apply {
            this.requestInterceptors += interceptor
        }

        /**
         * Adds a list of interceptors for processing responses.
         * @param interceptors List of response interceptors to add.
         * @return This builder instance.
         */
        @ExperimentalInterceptorsApi
        public fun responseInterceptors(interceptors: List<Interceptor<ServerMetadata>>): Builder = apply {
            this.responseInterceptors += interceptors
        }

        /**
         * Adds one or more interceptors for processing responses.
         * @param interceptors One or more response interceptors to add.
         * @return This builder instance.
         */
        @ExperimentalInterceptorsApi
        public fun responseInterceptors(vararg interceptors: Interceptor<ServerMetadata>): Builder = apply {
            this.responseInterceptors += interceptors
        }

        /**
         * Adds an interceptor for processing responses.
         * @param interceptor The response interceptor to add.
         * @return This builder instance.
         */
        @ExperimentalInterceptorsApi
        public fun responseInterceptor(interceptor: Interceptor<ServerMetadata>): Builder = apply {
            this.responseInterceptors += interceptor
        }

        /**
         * Sets the RSocket instance to use.
         * @param rsocket The RSocket instance.
         * @return This builder instance.
         */
        public fun rsocket(rsocket: RSocket): Builder = apply {
            this.rsocket = rsocket
        }

        /**
         * Appends the provided in the [builder] instances to existing ones.
         */
        @OptIn(InternalRSProtoAPI::class)
        public fun instances(builder: InstancesBuilder.() -> Unit): Builder = apply {
            val instances = InstancesBuilder().apply(builder).build()
            instances(instances)
        }

        /**
         * Appends the provided by [instances] parameter instances to existing ones.
         */
        public fun instances(instances: List<ProvidableInstance>): Builder = apply {
            instancesContainer = if (instancesContainer == null)
                InstanceContainer(instances.associateBy { it.key })
            else instancesContainer!! + instances
        }

        /**
         * Appends the provided [container] to the current one.
         */
        public fun instances(container: InstanceContainer): Builder = apply {
            instancesContainer = if (instancesContainer == null)
                container
            else instancesContainer!! + container
        }

        /**
         * Builds the final instance of [T] using the provided RSocket and interceptors.
         * @return The built instance of [T].
         */
        @OptIn(ExperimentalInterceptorsApi::class)
        public fun build(): RSPClientConfig {
            return RSPClientConfig(
                rsocket,
                Interceptors(requestInterceptors, responseInterceptors),
                instancesContainer ?: InstanceContainer(emptyMap()),
            )
        }
    }
}