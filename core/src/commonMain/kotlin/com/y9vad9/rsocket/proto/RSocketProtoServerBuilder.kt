package com.y9vad9.rsocket.proto

import com.y9vad9.rsocket.proto.annotations.ExperimentalInterceptorsApi
import com.y9vad9.rsocket.proto.interceptors.Interceptor
import com.y9vad9.rsocket.proto.services.RSocketService

public class RSocketProtoServerBuilder internal constructor() {
    public fun addService(service: RSocketService): RSocketProtoServerBuilder = apply {}

    @ExperimentalInterceptorsApi
    public fun addInterceptor(interceptor: Interceptor): RSocketProtoServerBuilder = apply {}

    public fun build(): RSocketProtoServer { TODO() }
}