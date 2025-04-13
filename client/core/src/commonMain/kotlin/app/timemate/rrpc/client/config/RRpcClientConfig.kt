@file:OptIn(ExperimentalInterceptorsApi::class)

package app.timemate.rrpc.client.config

import io.rsocket.kotlin.RSocket
import kotlinx.serialization.ExperimentalSerializationApi
import app.timemate.rrpc.annotations.ExperimentalInterceptorsApi
import app.timemate.rrpc.annotations.InternalRRpcAPI
import app.timemate.rrpc.instances.*
import app.timemate.rrpc.interceptors.RRpcInterceptor
import app.timemate.rrpc.interceptors.Interceptors
import app.timemate.rrpc.interceptors.InterceptorsBuilder
import app.timemate.rrpc.metadata.ClientMetadata
import app.timemate.rrpc.metadata.ServerMetadata
import kotlin.properties.Delegates

public data class RRpcClientConfig @OptIn(ExperimentalInterceptorsApi::class) constructor(
    public val rsocket: RSocket,
    public val interceptors: Interceptors,
    public val instances: InstanceContainer,
) {
    public companion object {
        public fun create(block: Builder.() -> Unit): RRpcClientConfig {
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
        /** The RSocket instance to use */
        private var rsocket: RSocket by Delegates.notNull()

        /** The container of provided instances */
        private var instancesContainer: InstanceContainer? = null

        private var interceptorsBuilder: InterceptorsBuilder = InterceptorsBuilder()

        /**
         * Specifies the request / response interceptors. Experimental feature
         * due to possible API changes.
         */
        @ExperimentalInterceptorsApi
        public fun interceptors(block: InterceptorsBuilder.() -> Unit) {
            interceptorsBuilder.block()
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
        @OptIn(InternalRRpcAPI::class)
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

        @OptIn(ExperimentalInterceptorsApi::class)
        public fun build(): RRpcClientConfig {
            return RRpcClientConfig(
                rsocket,
                interceptorsBuilder.build(),
                instancesContainer ?: InstanceContainer(emptyMap()),
            )
        }
    }
}