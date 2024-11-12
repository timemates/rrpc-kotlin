package org.timemates.rrpc.server

import org.timemates.rrpc.instances.InstanceContainer
import org.timemates.rrpc.interceptors.InterceptorContext
import org.timemates.rrpc.metadata.ClientMetadata
import org.timemates.rrpc.options.OptionsWithValue

/**
 * Represents the context of a request, encapsulating relevant data such as
 * instances, metadata, and options for configuring the request behavior.
 *
 * @property instances A container for instances relevant to this request.
 * @property metadata Metadata that provides client-specific information.
 * @property options Configurable on schema-level, options that may affect request handling.
 */
public data class RequestContext(
    public val instances: InstanceContainer,
    public val metadata: ClientMetadata,
    public val options: OptionsWithValue,
)

internal fun InterceptorContext<ClientMetadata>.toRequestContext(): RequestContext {
    return RequestContext(
        instances, metadata, options,
    )
}