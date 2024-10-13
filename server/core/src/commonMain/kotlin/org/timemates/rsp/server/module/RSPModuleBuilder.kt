package org.timemates.rrpc.server.module

import kotlinx.serialization.ExperimentalSerializationApi
import org.timemates.rrpc.annotations.ExperimentalInterceptorsApi
import org.timemates.rrpc.annotations.InternalRRpcrotoAPI
import org.timemates.rrpc.instances.InstanceContainer
import org.timemates.rrpc.instances.InstancesBuilder
import org.timemates.rrpc.instances.ProvidableInstance
import org.timemates.rrpc.instances.protobuf
import org.timemates.rrpc.interceptors.Interceptors
import org.timemates.rrpc.interceptors.RequestInterceptor
import org.timemates.rrpc.interceptors.ResponseInterceptor

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
@OptIn(InternalRRpcrotoAPI::class)
public class RRpcModuleBuilder internal constructor() {
    private val instances: MutableList<ProvidableInstance> = mutableListOf()
    private val services: MutableList<RRpcService> = mutableListOf()
    private val requestInterceptors: MutableList<RequestInterceptor> = mutableListOf()
    private val responseInterceptors: MutableList<ResponseInterceptor> = mutableListOf()

    /**
     * Adds a service to the module.
     *
     * @param service The [RRpcService] to add.
     */
    public fun service(service: RRpcService) {
        services += service
    }

    /**
     * Adds a request interceptor to the module.
     *
     * @param interceptor The [RequestInterceptor] to add.
     */
    @ExperimentalInterceptorsApi
    public fun requestInterceptor(interceptor: RequestInterceptor) {
        requestInterceptors += interceptor
    }

    /**
     * Adds a response interceptor to the module.
     *
     * @param interceptor The [ResponseInterceptor] to add.
     */
    @ExperimentalInterceptorsApi
    public fun responseInterceptor(interceptor: ResponseInterceptor) {
        responseInterceptors += interceptor
    }

    /**
     * Configures instances for the module using the provided [block].
     *
     * @param block A lambda with receiver of type [InstancesBuilder] used to configure instances.
     */
    public fun instances(block: InstancesBuilder.() -> Unit) {
        instances += InstancesBuilder().apply(block).build()
    }

    /**
     * Builds the [RRpcModule] with the configured services, interceptors, and instances.
     *
     * @return A configured [RRpcModule].
     */
    public fun build(): RRpcModule {
        return RRpcModuleImpl(
            services = services.map { it.descriptor },
            interceptors = Interceptors(requestInterceptors, responseInterceptors),
            instanceContainer = InstanceContainer(instances.associateBy { it.key }),
        )
    }
}

/**
 * Adds multiple request interceptors to the module.
 *
 * @param interceptors Vararg of [RequestInterceptor] to add.
 */
@ExperimentalInterceptorsApi
public fun RRpcModuleBuilder.requestInterceptors(vararg interceptors: RequestInterceptor) {
    interceptors.forEach { requestInterceptor(it) }
}

/**
 * Adds multiple response interceptors to the module.
 *
 * @param interceptors Vararg of [ResponseInterceptor] to add.
 */
@ExperimentalInterceptorsApi
public fun RRpcModuleBuilder.responseInterceptors(vararg interceptors: ResponseInterceptor) {
    interceptors.forEach { responseInterceptor(it) }
}
