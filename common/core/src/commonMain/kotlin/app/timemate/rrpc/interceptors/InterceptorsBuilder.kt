package app.timemate.rrpc.interceptors

import app.timemate.rrpc.annotations.ExperimentalInterceptorsApi

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

/**
 * Adds multiple request interceptors to the module.
 *
 * @param interceptors Vararg of [RequestRRpcInterceptor] to add.
 */
@ExperimentalInterceptorsApi
public fun InterceptorsBuilder.request(vararg interceptors: RequestRRpcInterceptor) {
    interceptors.forEach { request(it) }
}

/**
 * Adds multiple response interceptors to the module.
 *
 * @param interceptors Vararg of [ResponseRRpcInterceptor] to add.
 */
@ExperimentalInterceptorsApi
public fun InterceptorsBuilder.response(vararg interceptors: ResponseRRpcInterceptor) {
    interceptors.forEach { response(interceptor = it) }
}
