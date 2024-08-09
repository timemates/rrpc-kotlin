package org.timemates.rsp.server.module

import kotlinx.serialization.ExperimentalSerializationApi
import org.timemates.rsp.annotations.ExperimentalInterceptorsApi
import org.timemates.rsp.annotations.InternalRSProtoAPI
import org.timemates.rsp.instances.InstanceContainer
import org.timemates.rsp.instances.InstancesBuilder
import org.timemates.rsp.instances.ProvidableInstance
import org.timemates.rsp.instances.protobuf
import org.timemates.rsp.interceptors.Interceptors
import org.timemates.rsp.interceptors.RequestInterceptor
import org.timemates.rsp.interceptors.ResponseInterceptor

/**
 * Creates an [RSPModule] using the provided [block] to configure its builder.
 *
 * @param block A lambda with receiver of type [RSPModuleBuilder] used to configure the module.
 * @return A configured [RSPModule].
 */
@OptIn(ExperimentalSerializationApi::class)
public fun RSPModule(block: RSPModuleBuilder.() -> Unit): RSPModule {
    return RSPModuleBuilder().apply {
        instances {
            protobuf {
                encodeDefaults = true
            }
        }
    }.apply(block).build()
}

/**
 * Builder class for constructing an [RSPModule].
 */
@OptIn(InternalRSProtoAPI::class)
public class RSPModuleBuilder internal constructor() {
    private val instances: MutableList<ProvidableInstance> = mutableListOf()
    private val services: MutableList<RSPService> = mutableListOf()
    private val requestInterceptors: MutableList<RequestInterceptor> = mutableListOf()
    private val responseInterceptors: MutableList<ResponseInterceptor> = mutableListOf()

    /**
     * Adds a service to the module.
     *
     * @param service The [RSPService] to add.
     */
    public fun service(service: RSPService) {
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
     * Builds the [RSPModule] with the configured services, interceptors, and instances.
     *
     * @return A configured [RSPModule].
     */
    public fun build(): RSPModule {
        return RSPModuleImpl(
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
public fun RSPModuleBuilder.requestInterceptors(vararg interceptors: RequestInterceptor) {
    interceptors.forEach { requestInterceptor(it) }
}

/**
 * Adds multiple response interceptors to the module.
 *
 * @param interceptors Vararg of [ResponseInterceptor] to add.
 */
@ExperimentalInterceptorsApi
public fun RSPModuleBuilder.responseInterceptors(vararg interceptors: ResponseInterceptor) {
    interceptors.forEach { responseInterceptor(it) }
}
