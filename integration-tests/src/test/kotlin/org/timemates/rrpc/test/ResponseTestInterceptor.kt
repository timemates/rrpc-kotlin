package org.timemates.rrpc.test

import app.timemate.rrpc.annotations.ExperimentalInterceptorsApi
import app.timemate.rrpc.instances.ProvidableInstance
import app.timemate.rrpc.interceptors.InterceptorContext
import app.timemate.rrpc.interceptors.RRpcInterceptor
import app.timemate.rrpc.interceptors.modify
import app.timemate.rrpc.metadata.RRpcMetadata
import app.timemate.rrpc.metadata.ServerMetadata

@OptIn(ExperimentalInterceptorsApi::class)
class ResponseTestInterceptor : RRpcInterceptor<ServerMetadata> {
    override suspend fun intercept(context: InterceptorContext<ServerMetadata>): InterceptorContext<ServerMetadata> {
        assert(context.instances.getInstance(SomeValue)?.value == 1)
        return context.modify {
            addLocalInstance(SomeValue(2))
        }
    }
}