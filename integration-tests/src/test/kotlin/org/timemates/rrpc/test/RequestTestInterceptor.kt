package org.timemates.rrpc.test

import app.timemate.rrpc.annotations.ExperimentalInterceptorsApi
import app.timemate.rrpc.interceptors.InterceptorContext
import app.timemate.rrpc.interceptors.RRpcInterceptor
import app.timemate.rrpc.interceptors.modify
import app.timemate.rrpc.metadata.ClientMetadata

@OptIn(ExperimentalInterceptorsApi::class)
class RequestTestInterceptor : RRpcInterceptor<ClientMetadata> {
    override suspend fun intercept(context: InterceptorContext<ClientMetadata>): InterceptorContext<ClientMetadata> {
        assert(context.instances.getInstance(SomeValue)?.value == 0)
        return context.modify {
            addLocalInstance(SomeValue(1))
        }
    }

}

