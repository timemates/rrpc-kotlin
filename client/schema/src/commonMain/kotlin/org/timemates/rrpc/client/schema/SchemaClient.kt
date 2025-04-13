package app.timemate.rrpc.client.schema

import app.timemate.rrpc.annotations.InternalRRpcAPI
import app.timemate.rrpc.client.RRpcServiceClient
import app.timemate.rrpc.client.config.RRpcClientConfig
import app.timemate.rrpc.client.options.RPCsOptions
import app.timemate.rrpc.client.schema.request.PagedRequest
import app.timemate.rrpc.common.schema.RSFile
import app.timemate.rrpc.common.schema.RSService
import app.timemate.rrpc.metadata.ClientMetadata
import app.timemate.rrpc.options.OptionsWithValue
import app.timemate.rrpc.client.schema.request.BatchedRequest
import app.timemate.rrpc.common.schema.RSType
import io.rsocket.kotlin.RSocketError

/**
 * A client to interact with the `SchemaService` of the server, which provides metadata about
 * available services, types, and extensions. This client sends requests to the server and
 * receives the corresponding responses using the RSocket-based communication framework.
 *
 * @param config The configuration for the `RRpcServiceClient`, which defines connection settings.
 */
@OptIn(InternalRRpcAPI::class)
public class SchemaClient(
    config: RRpcClientConfig,
) : RRpcServiceClient(config) {

    public companion object {
        private const val SERVICE_NAME: String = "timemates.rrpc.server.schema.SchemaService"
    }

    /**
     * Creates an instance of the client using a configuration builder.
     *
     * @param creator A lambda to configure and build the `RRpcClientConfig` object.
     */
    public constructor(
        creator: RRpcClientConfig.Builder.() -> Unit,
    ) : this(RRpcClientConfig.create(creator))

    override val rpcsOptions: RPCsOptions = RPCsOptions.EMPTY

    /**
     * Fetches a paged list of available services from the server.
     *
     * @param request A [PagedRequest] defining pagination settings.
     * @return A [PagedRequest.Response] containing a list of [RSService] and the next page token.
     *
     * @throws RSocketError if the request fails.
     */
    public suspend fun getAvailableServices(request: PagedRequest): PagedRequest.Response<RSService> {
        return handler.requestResponse(
            metadata = ClientMetadata(
                serviceName = SERVICE_NAME,
                procedureName = "GetAvailableServices",
            ),
            data = request,
            options = OptionsWithValue.EMPTY,
            serializationStrategy = PagedRequest.serializer(),
            deserializationStrategy = PagedRequest.Response.serializer(RSService.serializer()),
        )
    }

    /**
     * Fetches a paged list of available files from the server.
     *
     * @param request A [PagedRequest] specifying pagination options.
     * @return A [PagedRequest.Response] containing a list of [RSFile] and the next page token.
     *
     * @throws RSocketError if the request fails.
     */
    public suspend fun getAvailableFiles(request: PagedRequest): PagedRequest.Response<RSFile> {
        return handler.requestResponse(
            metadata = ClientMetadata(
                serviceName = SERVICE_NAME,
                procedureName = "GetAvailableFiles",
            ),
            data = request,
            options = OptionsWithValue.EMPTY,
            serializationStrategy = PagedRequest.serializer(),
            deserializationStrategy = PagedRequest.Response.serializer(RSFile.serializer()),
        )
    }

    /**
     * Retrieves detailed information about multiple types in a batched request.
     *
     * @param request A [BatchedRequest] containing a list of [RMDeclarationUrl]s for the requested types.
     * @return A [BatchedRequest.Response] containing a map of each requested [RMDeclarationUrl] to its associated [RSType].
     *
     * @throws RSocketError if the request fails.
     */
    public suspend fun getTypeDetailsBatch(request: BatchedRequest): BatchedRequest.Response<RSType> {
        return handler.requestResponse(
            metadata = ClientMetadata(
                serviceName = SERVICE_NAME,
                procedureName = "GetTypeDetailsBatch",
            ),
            data = request,
            options = OptionsWithValue.EMPTY,
            serializationStrategy = BatchedRequest.serializer(),
            deserializationStrategy = BatchedRequest.Response.serializer(RSType.serializer()),
        )
    }
}
