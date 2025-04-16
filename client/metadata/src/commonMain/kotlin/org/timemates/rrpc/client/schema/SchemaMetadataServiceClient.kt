package org.timemates.rrpc.client.schema

import app.timemate.rrpc.annotations.InternalRRpcAPI
import app.timemate.rrpc.client.RRpcServiceClient
import app.timemate.rrpc.client.config.RRpcClientConfig
import app.timemate.rrpc.client.options.RPCsOptions
import app.timemate.rrpc.metadata.ClientMetadata
import app.timemate.rrpc.metadata.ExtraMetadata
import app.timemate.rrpc.metadata.common.communication.GetAllExtendsRequest
import app.timemate.rrpc.metadata.common.communication.GetAllFilesRequest
import app.timemate.rrpc.metadata.common.communication.GetAllServicesRequest
import app.timemate.rrpc.metadata.common.communication.GetAllTypesRequest
import app.timemate.rrpc.metadata.common.communication.GetDeclarationRequest
import app.timemate.rrpc.metadata.common.communication.GetMetadataVersionRequest
import app.timemate.rrpc.options.OptionsWithValue
import kotlinx.coroutines.flow.Flow

@OptIn(InternalRRpcAPI::class)
public class SchemaMetadataServiceClient(
    config: RRpcClientConfig
) : RRpcServiceClient(config) {

    override val rpcsOptions: RPCsOptions = RPCsOptions.EMPTY
    override val serviceName: String = "timemates.rrpc.server.schema.MetadataService"

    /**
     * Fetches all files in a paginated manner.
     *
     * @param request The request containing page size and token information.
     * @param extra Additional metadata that can be passed for the request.
     * @return A Flow emitting paginated [GetAllFilesRequest.Response] objects.
     */
    public fun getAllFiles(
        request: GetAllFilesRequest,
        extra: Map<String, ByteArray> = emptyMap()
    ): Flow<GetAllFilesRequest.Response> = handler.requestStream(
        ClientMetadata(
            serviceName = this.serviceName,
            procedureName = "GetAllFiles",
            extra = ExtraMetadata(extra)
        ),
        data = request,
        options = rpcsOptions["GetAllFiles"] ?: OptionsWithValue.EMPTY,
        serializationStrategy = GetAllFilesRequest.serializer(),
        deserializationStrategy = GetAllFilesRequest.Response.serializer()
    )

    /**
     * Fetches all types in a paginated manner.
     *
     * @param request The request containing page size and token information.
     * @param extra Additional metadata that can be passed for the request.
     * @return A Flow emitting paginated [GetAllTypesRequest.Response] objects.
     */
    public fun getAllTypes(
        request: GetAllTypesRequest,
        extra: Map<String, ByteArray> = emptyMap()
    ): Flow<GetAllTypesRequest.Response> = handler.requestStream(
        ClientMetadata(
            serviceName = this.serviceName,
            procedureName = "GetAllTypes",
            extra = ExtraMetadata(extra)
        ),
        data = request,
        options = rpcsOptions["GetAllTypes"] ?: OptionsWithValue.EMPTY,
        serializationStrategy = GetAllTypesRequest.serializer(),
        deserializationStrategy = GetAllTypesRequest.Response.serializer()
    )

    /**
     * Fetches all services in a paginated manner.
     *
     * @param request The request containing page size and token information.
     * @param extra Additional metadata that can be passed for the request.
     * @return A Flow emitting paginated [GetAllServicesRequest.Response] objects.
     */
    public fun getAllServices(
        request: GetAllServicesRequest,
        extra: Map<String, ByteArray> = emptyMap()
    ): Flow<GetAllServicesRequest.Response> = handler.requestStream(
        ClientMetadata(
            serviceName = this.serviceName,
            procedureName = "GetAllServices",
            extra = ExtraMetadata(extra)
        ),
        data = request,
        options = rpcsOptions["GetAllServices"] ?: OptionsWithValue.EMPTY,
        serializationStrategy = GetAllServicesRequest.serializer(),
        deserializationStrategy = GetAllServicesRequest.Response.serializer()
    )

    /**
     * Fetches all extensions (RSExtends) in a paginated manner.
     *
     * @param request The request containing page size and token information.
     * @param extra Additional metadata that can be passed for the request.
     * @return A Flow emitting paginated [GetAllExtendsRequest.Response] objects.
     */
    public fun getAllExtends(
        request: GetAllExtendsRequest,
        extra: Map<String, ByteArray> = emptyMap()
    ): Flow<GetAllExtendsRequest.Response> = handler.requestStream(
        ClientMetadata(
            serviceName = this.serviceName,
            procedureName = "GetAllExtends",
            extra = ExtraMetadata(extra)
        ),
        data = request,
        options = rpcsOptions["GetAllExtends"] ?: OptionsWithValue.EMPTY,
        serializationStrategy = GetAllExtendsRequest.serializer(),
        deserializationStrategy = GetAllExtendsRequest.Response.serializer()
    )

    /**
     * Fetches metadata version.
     *
     * @param request The request containing version information.
     * @param extra Additional metadata that can be passed for the request.
     * @return The metadata version as a response.
     */
    public suspend fun getMetadataVersion(
        request: GetMetadataVersionRequest,
        extra: Map<String, ByteArray> = emptyMap()
    ): GetMetadataVersionRequest.Response = handler.requestResponse(
        ClientMetadata(
            serviceName = this.serviceName,
            procedureName = "GetMetadataVersion",
            extra = ExtraMetadata(extra)
        ),
        data = request,
        options = rpcsOptions["GetMetadataVersion"] ?: OptionsWithValue.EMPTY,
        serializationStrategy = GetMetadataVersionRequest.serializer(),
        deserializationStrategy = GetMetadataVersionRequest.Response.serializer()
    )

    /**
     * Fetches declaration by its URL.
     *
     * @param request The request containing the URL of the declaration.
     * @param extra Additional metadata that can be passed for the request.
     * @return The declaration response.
     */
    public suspend fun getDeclaration(
        request: GetDeclarationRequest,
        extra: Map<String, ByteArray> = emptyMap()
    ): GetDeclarationRequest.Response = handler.requestResponse(
        ClientMetadata(
            serviceName = this.serviceName,
            procedureName = "GetDeclaration",
            extra = ExtraMetadata(extra)
        ),
        data = request,
        options = rpcsOptions["GetDeclaration"] ?: OptionsWithValue.EMPTY,
        serializationStrategy = GetDeclarationRequest.serializer(),
        deserializationStrategy = GetDeclarationRequest.Response.serializer()
    )
}
