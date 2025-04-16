package org.timemates.rrpc.server.schema

import app.timemate.rrpc.metadata.common.communication.*
import app.timemate.rrpc.options.OptionsWithValue
import app.timemate.rrpc.server.RequestContext
import app.timemate.rrpc.server.module.RRpcService
import app.timemate.rrpc.server.module.descriptors.ProcedureDescriptor
import app.timemate.rrpc.server.module.descriptors.ServiceDescriptor
import kotlinx.coroutines.flow.Flow

public abstract class SchemaMetadataService : RRpcService {
    override val descriptor: ServiceDescriptor = ServiceDescriptor(
        name = "timemates.rrpc.server.schema.MetadataService",
        procedures = listOf(
            ProcedureDescriptor.RequestStream(
                name = "GetAllFiles",
                inputSerializer = GetAllFilesRequest.serializer(),
                outputSerializer = GetAllFilesRequest.Response.serializer(),
                procedure = { context, request -> getAllFiles(context, request) },
                options = OptionsWithValue.EMPTY,
            ),
            ProcedureDescriptor.RequestResponse(
                name = "GetDeclaration",
                inputSerializer = GetDeclarationRequest.serializer(),
                outputSerializer = GetDeclarationRequest.Response.serializer(),
                procedure = { context, request -> getDeclaration(context, request) },
                options = OptionsWithValue.EMPTY,
            ),
            ProcedureDescriptor.RequestStream(
                name = "GetAllTypes",
                inputSerializer = GetAllTypesRequest.serializer(),
                outputSerializer = GetAllTypesRequest.Response.serializer(),
                procedure = { context, request -> getAllTypes(context, request) },
                options = OptionsWithValue.EMPTY,
            ),
            ProcedureDescriptor.RequestStream(
                name = "GetAllServices",
                inputSerializer = GetAllServicesRequest.serializer(),
                outputSerializer = GetAllServicesRequest.Response.serializer(),
                procedure = { context, request -> getAllServices(context, request) },
                options = OptionsWithValue.EMPTY,
            ),
            ProcedureDescriptor.RequestResponse(
                name = "GetMetadataVersion",
                inputSerializer = GetMetadataVersionRequest.serializer(),
                outputSerializer = GetMetadataVersionRequest.Response.serializer(),
                procedure = { context, request -> getMetadataVersion(context, request) },
                options = OptionsWithValue.EMPTY,
            ),
        ),
        options = OptionsWithValue.EMPTY,
    )

    /**
     * Procedure implementation to fetch all files.
     */
    public abstract fun getAllFiles(
        context: RequestContext,
        request: GetAllFilesRequest,
    ): Flow<GetAllFilesRequest.Response>

    /**
     * Procedure implementation to fetch declaration by URL.
     */
    public abstract suspend fun getDeclaration(
        context: RequestContext,
        request: GetDeclarationRequest,
    ): GetDeclarationRequest.Response

    /**
     * Procedure implementation to fetch all types.
     */
    public abstract fun getAllTypes(
        context: RequestContext,
        request: GetAllTypesRequest,
    ): Flow<GetAllTypesRequest.Response>

    /**
     * Procedure implementation to fetch all services.
     */
    public abstract fun getAllServices(
        context: RequestContext,
        request: GetAllServicesRequest,
    ): Flow<GetAllServicesRequest.Response>

    /**
     * Procedure implementation to fetch metadata version.
     */
    public abstract suspend fun getMetadataVersion(
        context: RequestContext,
        request: GetMetadataVersionRequest,
    ): GetMetadataVersionRequest.Response

    /**
     * Procedure implementation to fetch all extensions (RSExtends) from the metadata.
     *
     * This function supports pagination, returning results in chunks based on the page size.
     * It uses a `pageToken` to track the state and provide continuous pagination when reconnected.
     *
     * @param context The request context.
     * @param request The paginated request containing the page size and the optional page token.
     * @return A [Flow] emitting paginated [GetAllExtendsRequest.Response] objects containing extension declarations.
     */
    public abstract suspend fun getAllExtends(
        context: RequestContext,
        request: GetAllExtendsRequest,
    ): Flow<GetAllExtendsRequest.Response>
}
