package app.timemate.rrpc.server.schema

import io.rsocket.kotlin.RSocketError
import app.timemate.rrpc.common.schema.RSFile
import app.timemate.rrpc.common.schema.RSService
import app.timemate.rrpc.common.schema.RSType
import app.timemate.rrpc.options.OptionsWithValue
import app.timemate.rrpc.server.module.RRpcService
import app.timemate.rrpc.server.module.descriptors.ProcedureDescriptor
import app.timemate.rrpc.server.module.descriptors.ServiceDescriptor
import app.timemate.rrpc.server.schema.request.BatchedRequest
import app.timemate.rrpc.server.schema.request.PagedRequest
import app.timemate.rrpc.server.schema.request.decoded
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Handles metadata-related operations for the RSocket-based reflection service.
 * This service exposes APIs that allow clients to query information about
 * available services, types, and extensions in the system.
 *
 * It supports both paged requests for larger datasets and batched requests
 * for retrieving multiple types of metadata in one go.
 *
 * @param group The metadata lookup group used for resolving services, types, and extensions.
 */
public class SchemaService(
    private val group: SchemaMetadata = SchemaMetadata.Global,
) : RRpcService {
    override val descriptor: ServiceDescriptor = ServiceDescriptor(
        name = "timemates.rrpc.server.schema.SchemaService",
        procedures = listOf(
            ProcedureDescriptor.RequestResponse(
                name = "GetAvailableServices",
                inputSerializer = PagedRequest.serializer(),
                outputSerializer = PagedRequest.Response.serializer(RSService.serializer()),
                procedure = { _, request -> getAvailableServices(request) },
                options = OptionsWithValue.EMPTY,
            ),
            ProcedureDescriptor.RequestResponse(
                name = "GetAvailableFiles",
                inputSerializer = PagedRequest.serializer(),
                outputSerializer = PagedRequest.Response.serializer(RSFile.serializer()),
                procedure = { _, request -> getAvailableFiles(request) },
                options = OptionsWithValue.EMPTY,
            ),
            ProcedureDescriptor.RequestResponse(
                name = "GetTypeDetailsBatch",
                inputSerializer = BatchedRequest.serializer(),
                outputSerializer = BatchedRequest.Response.serializer(RSType.serializer()),
                procedure = { _, request -> getTypeDetailsBatch(request) },
                options = OptionsWithValue.EMPTY,
            ),
        ),
        options = OptionsWithValue.EMPTY,
    )

    /**
     * Retrieves a paginated list of available services from the metadata.
     *
     * @param request The paged request containing the pagination token and size.
     * @return A paginated response containing a list of RMService objects.
     */
    public fun getAvailableServices(request: PagedRequest): PagedRequest.Response<RSService> {
        val decoded = request.decoded()?.split(":")
        val index = decoded?.getOrElse(1) { invalidPageToken() }?.toInt() ?: -1

        val result = group.resolveAllServices().drop(index + 1).take(request.size ?: 20).toList()

        return PagedRequest.Response.encoded(
            list = result,
            nextCursor = if (result.size == request.size) "c:${index + result.size}" else null
        )
    }

    /**
     * Retrieves available files that are loaded into [SchemaMetadata].
     *
     * @param request The paged request containing pagination information.
     * @return A paged response containing a list of available RMFiles.
     */
    @OptIn(ExperimentalEncodingApi::class)
    public fun getAvailableFiles(request: PagedRequest): PagedRequest.Response<RSFile> {
        val decoded = request.decoded()?.split(":")
        val index = decoded?.getOrElse(1) { invalidPageToken() }?.toInt() ?: -1

        val result = group.resolveAvailableFiles().drop(index + 1).take(30).toList()

        return PagedRequest.Response.encoded(
            list = result,
            nextCursor = if (result.size == index) "c:${index + result.size}" else null,
        )
    }

    /**
     * Retrieves type details based on a batch of requested URLs.
     *
     * Each RMDeclarationUrl is mapped to either the found RMType or null if no type was found.
     *
     * @param request The batched request containing a list of RMDeclarationUrl objects.
     * @return A response containing a map of RMDeclarationUrl to RMType (or null if not found).
     */
    public fun getTypeDetailsBatch(request: BatchedRequest): BatchedRequest.Response<RSType> {
        val types = request.urls.associateWith { group.resolveType(it) }
        return BatchedRequest.Response(types)
    }
}

private fun invalidPageToken(): Nothing = throw RSocketError.Invalid("Invalid page token")