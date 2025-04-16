package org.timemates.rrpc.server.schema

import app.timemate.rrpc.metadata.common.SchemaMetadataModule
import app.timemate.rrpc.metadata.common.communication.*
import app.timemate.rrpc.server.RequestContext
import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Default implementation of the [SchemaMetadataService], utilizing an in-memory schema metadata module.
 * This service performs operations like fetching files, types, services, and declarations by utilizing
 * the provided [SchemaMetadataModule] and supporting pagination via base64-encoded page tokens.
 *
 * @param schemaMetadataModule The schema metadata module that provides the actual schema data.
 */
public class DefaultSchemaMetadataService(
    private val schemaMetadataModule: SchemaMetadataModule,
) : SchemaMetadataService() {

    override fun getAllFiles(
        context: RequestContext,
        request: GetAllFilesRequest,
    ): Flow<GetAllFilesRequest.Response> = flow {
        val pageSize = request.pageSize.takeUnless { it <= 0 || it > 30 } ?: 30
        val pageToken = request.pageToken?.decodeBase64ToString() ?: "0"  // Default to first page
        var currentIndex = pageToken.toInt()

        // Fetch all files from the schema metadata module
        val allFiles = schemaMetadataModule.resolveAllFiles()

        // Continue emitting paginated responses until all files are processed
        while (currentIndex < allFiles.size) {
            val paginatedFiles = allFiles.drop(currentIndex).take(pageSize)

            // Determine the next page token (if there are more files)
            val nextPageToken = if (currentIndex + paginatedFiles.size < allFiles.size) {
                (currentIndex + paginatedFiles.size).toString().encodeToBase64()
            } else {
                null
            }

            // Emit the current page of files with the nextPageToken
            emit(
                GetAllFilesRequest.Response.create {
                    this.files = paginatedFiles
                    this.pageToken = nextPageToken
                }
            )

            // Update the current index to the next position
            currentIndex += paginatedFiles.size
        }
    }


    override suspend fun getDeclaration(
        context: RequestContext,
        request: GetDeclarationRequest,
    ): GetDeclarationRequest.Response {
        val result = request.urls.associateWith {
            schemaMetadataModule.resolveDeclaration(it)?.let {
                GetDeclarationRequest.Response.Result(it)
            } ?: GetDeclarationRequest.Response.Result.Default
        }

        return GetDeclarationRequest.Response(result)
    }

    override fun getAllTypes(
        context: RequestContext,
        request: GetAllTypesRequest,
    ): Flow<GetAllTypesRequest.Response> = flow {
        val pageSize = request.pageSize.takeUnless { it <= 0 || it > 30 } ?: 30
        val pageToken = request.pageToken?.decodeBase64ToString() ?: "0"  // Default to first page
        var currentIndex = pageToken.toInt()

        // Fetch all types from the schema metadata module
        val allTypes = schemaMetadataModule.resolveAllTypes()

        // Continue emitting paginated responses until all types are processed
        while (currentIndex < allTypes.size) {
            val paginatedTypes = allTypes.drop(currentIndex).take(pageSize)

            // Determine the next page token (if there are more types)
            val nextPageToken = if (currentIndex + paginatedTypes.size < allTypes.size) {
                (currentIndex + paginatedTypes.size).toString().encodeToBase64()
            } else {
                null
            }

            // Emit the current page of types with the nextPageToken
            emit(
                GetAllTypesRequest.Response.create {
                    this.types = paginatedTypes
                    this.pageToken = nextPageToken
                }
            )

            // Update the current index to the next position
            currentIndex += paginatedTypes.size
        }
    }

    override fun getAllServices(
        context: RequestContext,
        request: GetAllServicesRequest,
    ): Flow<GetAllServicesRequest.Response> = flow {
        val pageSize = request.pageSize.takeUnless { it <= 0 || it > 30 } ?: 30
        val pageToken = request.pageToken?.decodeBase64ToString() ?: "0"  // Default to first page
        var currentIndex = pageToken.toInt()

        // Fetch all services from the schema metadata module
        val allServices = schemaMetadataModule.resolveAllServices()

        // Continue emitting paginated responses until all services are processed
        while (currentIndex < allServices.size) {
            val paginatedServices = allServices.drop(currentIndex).take(pageSize)

            // Determine the next page token (if there are more services)
            val nextPageToken = if (currentIndex + paginatedServices.size < allServices.size) {
                (currentIndex + paginatedServices.size).toString().encodeToBase64()
            } else {
                null
            }

            // Emit the current page of services with the nextPageToken
            emit(
                GetAllServicesRequest.Response.create {
                    this.services = paginatedServices
                    this.pageToken = nextPageToken
                }
            )

            // Update the current index to the next position
            currentIndex += paginatedServices.size
        }
    }


    override suspend fun getMetadataVersion(
        context: RequestContext,
        request: GetMetadataVersionRequest,
    ): GetMetadataVersionRequest.Response {
        return GetMetadataVersionRequest.Response.create {
            currentVersion = 1.0
            minimumSupportedVersion = 1.0
        }
    }

    override suspend fun getAllExtends(
        context: RequestContext,
        request: GetAllExtendsRequest,
    ): Flow<GetAllExtendsRequest.Response> = flow {
        val pageSize = request.pageSize.takeUnless { it <= 0 || it > 30 } ?: 30
        var pageToken = request.pageToken?.decodeBase64ToString() ?: "0"  // Default to first page
        var currentIndex = pageToken.toInt()

        // Fetch all extensions (RSExtends) from the schema metadata module
        val allExtends = schemaMetadataModule.resolveAllExtends()

        // Continue emitting paginated responses until all extensions are processed
        while (currentIndex < allExtends.size) {
            val paginatedExtends = allExtends.drop(currentIndex).take(pageSize)

            // Determine the next page token (if there are more extensions to return)
            val nextPageToken = if (currentIndex + paginatedExtends.size < allExtends.size) {
                (currentIndex + paginatedExtends.size).toString().encodeToBase64()
            } else {
                null
            }

            // Emit the current page of extension declarations with the nextPageToken
            emit(
                GetAllExtendsRequest.Response.create {
                    this.extends = paginatedExtends
                    this.pageToken = nextPageToken
                }
            )

            // Update the current index to the next position
            currentIndex += paginatedExtends.size
        }
    }

}


@OptIn(ExperimentalEncodingApi::class)
private fun String.decodeBase64ToString(): String? =
    Base64.decode(this).decodeToString()

@OptIn(ExperimentalEncodingApi::class)
private fun String.encodeToBase64(): String =
    Base64.encode(this.toByteArray())
