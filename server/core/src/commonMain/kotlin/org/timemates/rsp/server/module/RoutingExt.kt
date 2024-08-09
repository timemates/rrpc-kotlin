package org.timemates.rsp.server.module

import io.ktor.server.routing.*
import io.rsocket.kotlin.RSocketRequestHandler
import io.rsocket.kotlin.ktor.server.rSocket

/**
 * Creates and configures an RSocket server with the specified endpoint and RSocketProtoServer.
 *
 * @param endpoint The endpoint to bind the server to. Default value is `/rsp`.
 * @param module The module that will be handling the incoming requests.
 */
public fun Routing.rspEndpoint(endpoint: String = "/rsp", module: RSPModule) {
    rSocket(endpoint) {
        RSocketRequestHandler {
            RSPModuleHandler(module).setup(this)
        }
    }
}

/**
 * Creates an RSocket server endpoint on the specified routing path.
 *
 * @param endpoint The routing path for the RSocket server.
 * @param block The configuration block for the RSPModuleBuilder.
 */
public fun Routing.rspEndpoint(endpoint: String = "/rsp", block: RSPModuleBuilder.() -> Unit) {
    rspEndpoint(endpoint, RSPModuleBuilder().apply(block).build())
}
