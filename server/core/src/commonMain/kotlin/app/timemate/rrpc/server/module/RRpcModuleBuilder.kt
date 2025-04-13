package app.timemate.rrpc.server.module

import kotlinx.serialization.ExperimentalSerializationApi
import app.timemate.rrpc.annotations.ExperimentalInterceptorsApi
import app.timemate.rrpc.annotations.InternalRRpcAPI
import app.timemate.rrpc.instances.InstanceContainer
import app.timemate.rrpc.instances.InstancesBuilder
import app.timemate.rrpc.instances.protobuf
import app.timemate.rrpc.interceptors.Interceptors
import app.timemate.rrpc.interceptors.RequestRRpcInterceptor
import app.timemate.rrpc.interceptors.ResponseRRpcInterceptor

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

    @ExperimentalInterceptorsApi
    public class InterceptorsBuilder {
        private val requestInterceptors: MutableList<RequestRRpcInterceptor> = mutableListOf()
        private val responseInterceptors: MutableList<ResponseRRpcInterceptor> = mutableListOf()

        /**
         * Adds a request interceptor to the module.
         *
         * @param interceptor The [RequestRRpcInterceptor] to add.
         */
        @ExperimentalInterceptorsApi
        public fun request(interceptor: RequestRRpcInterceptor) {
            requestInterceptors += interceptor
        }

        /**
         * Adds a response interceptor to the module.
         *
         * @param interceptor The [ResponseRRpcInterceptor] to add.
         */
        @ExperimentalInterceptorsApi
        public fun response(interceptor: ResponseRRpcInterceptor) {
            responseInterceptors += interceptor
        }

        @ExperimentalInterceptorsApi
        public fun build(): Interceptors = Interceptors(requestInterceptors.toList(), responseInterceptors.toList())
    }
}

/**
 * Adds multiple request interceptors to the module.
 *
 * @param interceptors Vararg of [RequestRRpcInterceptor] to add.
 */
@ExperimentalInterceptorsApi
public fun RRpcModuleBuilder.InterceptorsBuilder.request(vararg interceptors: RequestRRpcInterceptor) {
    interceptors.forEach { request(it) }
}

/**
 * Adds multiple response interceptors to the module.
 *
 * @param interceptors Vararg of [ResponseRRpcInterceptor] to add.
 */
@ExperimentalInterceptorsApi
public fun RRpcModuleBuilder.InterceptorsBuilder.response(vararg interceptors: ResponseRRpcInterceptor) {
    interceptors.forEach { response(interceptor = it) }
}
