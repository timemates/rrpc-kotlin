package io.timemates.rsproto.server

import io.timemates.rsproto.server.annotations.ExperimentalInstancesApi
import io.timemates.rsproto.server.annotations.ExperimentalInterceptorsApi
import io.timemates.rsproto.server.instances.ProvidableInstance
import io.timemates.rsproto.server.instances.protobuf
import io.timemates.rsproto.server.interceptors.Interceptor
import kotlinx.serialization.ExperimentalSerializationApi

public class RSocketProtoServerBuilder internal constructor() {
    @OptIn(ExperimentalInstancesApi::class)
    private val instances: MutableList<ProvidableInstance> = mutableListOf()
    private val services: MutableList<RSocketService> = mutableListOf()
    @OptIn(ExperimentalInterceptorsApi::class)
    private val interceptors: MutableList<Interceptor> = mutableListOf()

    public fun service(service: RSocketService) {
        services += service
    }

    @ExperimentalInterceptorsApi
    public fun interceptor(interceptor: Interceptor) {
        interceptors += interceptor
    }

    @ExperimentalInstancesApi
    public fun instances(block: InstancesBuilder.() -> Unit) {
        instances += InstancesBuilder().apply(block).build()
    }

    @OptIn(ExperimentalInterceptorsApi::class, ExperimentalInstancesApi::class)
    public fun build(): RSocketProtoServer {
        return RSocketProtoServerImpl(
            services = services.map { it.descriptor },
            interceptors = interceptors.toList(),
            instances = instances.associateBy { it.key },
        )
    }

    @ExperimentalInstancesApi
    public class InstancesBuilder internal constructor() {
        private val instances: MutableList<ProvidableInstance> = mutableListOf()

        public fun register(instance: ProvidableInstance) {
            instances += instance
        }

        internal fun build(): List<ProvidableInstance> {
            return instances.toList()
        }
    }
}

@OptIn(ExperimentalInstancesApi::class, ExperimentalSerializationApi::class)
public fun RSocketProtoServer(block: RSocketProtoServerBuilder.() -> Unit): RSocketProtoServer {
    return RSocketProtoServerBuilder().apply {
        instances {
            protobuf {
                encodeDefaults = true
            }
        }
    }.apply(block).build()
}