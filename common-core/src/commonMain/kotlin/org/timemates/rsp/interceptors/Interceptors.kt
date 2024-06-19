package org.timemates.rsp.interceptors

import org.timemates.rsp.annotations.ExperimentalInterceptorsApi

@ExperimentalInterceptorsApi
public data class Interceptors(
    public val forRequests: List<Interceptor.Request>,
    public val forResponses: List<Interceptor.Response>
)