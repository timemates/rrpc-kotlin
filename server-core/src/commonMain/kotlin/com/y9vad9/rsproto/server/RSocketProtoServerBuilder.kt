package com.y9vad9.rsproto.server

import com.y9vad9.rsproto.server.annotations.ExperimentalInstancesApi
import com.y9vad9.rsproto.server.annotations.ExperimentalInterceptorsApi
import com.y9vad9.rsproto.server.interceptors.Interceptor
import com.y9vad9.rsproto.server.instances.ProvidableInstance
import com.y9vad9.rsproto.server.instances.protobuf
import kotlinx.serialization.ExperimentalSerializationApi

public class RSocketProtoServerBuilder internal constructor() {
    @OptIn(ExperimentalInstancesApi::class)
    private val instances: MutableList<ProvidableInstance> = mutableListOf()

    public fun service(service: RSocketService): RSocketProtoServerBuilder = apply {}

    @ExperimentalInterceptorsApi
    public fun interceptor(interceptor: Interceptor): RSocketProtoServerBuilder = apply {}

    @ExperimentalInstancesApi
    public fun instances(block: InstancesBuilder.() -> Unit) {
        instances += InstancesBuilder().apply(block).build()
    }

    public fun build(): RSocketProtoServer {
        TODO()
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