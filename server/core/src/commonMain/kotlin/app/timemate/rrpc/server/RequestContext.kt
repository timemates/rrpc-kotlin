package app.timemate.rrpc.server

import app.timemate.rrpc.instances.InstanceContainer
import app.timemate.rrpc.interceptors.InterceptorContext
import app.timemate.rrpc.metadata.ClientMetadata
import app.timemate.rrpc.options.OptionsWithValue

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