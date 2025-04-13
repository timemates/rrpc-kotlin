package app.timemate.rrpc.server.module

import app.timemate.rrpc.annotations.ExperimentalInterceptorsApi
import app.timemate.rrpc.annotations.InternalRRpcAPI
import app.timemate.rrpc.instances.InstanceContainer
import app.timemate.rrpc.instances.InstancesBuilder
import app.timemate.rrpc.instances.protobuf
import app.timemate.rrpc.interceptors.InterceptorsBuilder
import kotlinx.serialization.ExperimentalSerializationApi

/**
 * Creates an [RRpcModule] using the provided [block] to configure its builder.
 *
 * @param block A lambda with receiver of type [RRpcModuleBuilder] used to configure the module.
 * @return A configured [RRpcModule].
 */
@OptIn(ExperimentalSerializationApi::class)
public fun RRpcModule(block: RRpcModuleBuilder.() -> Unit): RRpcModule {
    return RRpcModuleBuilder().apply {
        instances {
            protobuf {
                encodeDefaults = true
            }
        }
    }.apply(block).build()
}

/**
 * Builder class for constructing an [RRpcModule].
 */
@OptIn(InternalRRpcAPI::class)
public class RRpcModuleBuilder internal constructor() {
    private val instances: InstancesBuilder = InstancesBuilder()
    private val services: ServicesBuilder = ServicesBuilder()

    @OptIn(ExperimentalInterceptorsApi::class)
    private val interceptors: InterceptorsBuilder = InterceptorsBuilder()

    public fun services(builder: ServicesBuilder.() -> Unit) {
        services.apply(builder).build()
    }

    @ExperimentalInterceptorsApi
    public fun interceptors(builder: InterceptorsBuilder.() -> Unit) {
        interceptors.builder()
    }

    /**
     * Configures instances for the module using the provided [block].
     *
     * @param block A lambda with receiver of type [InstancesBuilder] used to configure instances.
     */
    public fun instances(block: InstancesBuilder.() -> Unit) {
        instances.apply(block).build()
    }

    /**
     * Builds the [RRpcModule] with the configured services, interceptors, and instances.
     *
     * @return A configured [RRpcModule].
     */
    @OptIn(ExperimentalInterceptorsApi::class)
    public fun build(): RRpcModule {
        return RRpcModuleImpl(
            services = services.build().map { it.descriptor },
            interceptors = interceptors.build(),
            instanceContainer = InstanceContainer(instances.build().associateBy { it.key }),
        )
    }

    public class ServicesBuilder {
        private val services: MutableList<RRpcService> = mutableListOf()

        public fun register(service: RRpcService) {
            services += service
        }

        public fun build(): List<RRpcService> = services.toList()
    }
}