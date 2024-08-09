package org.timemates.rsp.server

import org.timemates.rsp.instances.InstanceContainer
import org.timemates.rsp.interceptors.InterceptorContext
import org.timemates.rsp.metadata.ClientMetadata
import org.timemates.rsp.metadata.RSPMetadata
import org.timemates.rsp.options.Options

public data class RequestContext(
    public val instances: InstanceContainer,
    public val metadata: ClientMetadata,
    public val options: Options,
)

internal fun InterceptorContext<ClientMetadata>.toRequestContext(): RequestContext {
    return RequestContext(
        instances, metadata, options,
    )
}