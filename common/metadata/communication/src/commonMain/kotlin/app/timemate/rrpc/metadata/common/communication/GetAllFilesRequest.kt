package app.timemate.rrpc.metadata.common.communication

import app.timemate.rrpc.RSProtoType
import app.timemate.rrpc.proto.schema.RSFile
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kotlin.jvm.JvmField

/**
 * A request message used to fetch all available `.rs` schema files in a paginated form.
 *
 * This message is intended to be used with a metadata service endpoint like `rpc GetAllFiles(GetAllFilesRequest): Response`.
 * It allows the client to paginate through the files, avoiding large payloads in a single request.
 *
 * @property pageSize The number of files to return in this request. If `0`, the service decides the chunk size.
 * @property pageToken A token identifying the current pagination position. Initially null; provided by the service.
 */
@Serializable
public class GetAllFilesRequest private constructor(
    @ProtoNumber(1)
    public val pageSize: Int = 0,
    @ProtoNumber(2)
    public val pageToken: String?,
) : RSProtoType {

    override val definition: RSProtoType.Definition<*>
        get() = Companion

    public companion object : RSProtoType.Definition<GetAllFilesRequest> {
        override val url: String = "type.googleapis.com/app.timemate.metadata.request.GetAllFiles"
        override val Default: GetAllFilesRequest = GetAllFilesRequest(pageToken = null)

        /**
         * Creates a new [GetAllFilesRequest] using the provided DSL builder block.
         *
         * This function allows constructing requests in a more idiomatic and test-friendly way.
         *
         * Example:
         * ```
         * val request = GetAllFilesRequest.create {
         *     pageSize = 50
         *     pageToken = "token-123"
         * }
         * ```
         *
         * @param builder Lambda with receiver for configuring the request
         * @return the constructed [GetAllFilesRequest]
         */
        public fun create(builder: Builder.() -> Unit): GetAllFilesRequest {
            return Builder().apply(builder).build()
        }

        /**
         * DSL-style builder for [GetAllFilesRequest].
         */
        public class Builder {
            @JvmField
            public var pageSize: Int = 0
            @JvmField
            public var pageToken: String? = null

            public fun build(): GetAllFilesRequest = GetAllFilesRequest(
                pageSize = pageSize,
                pageToken = pageToken
            )
        }
    }

    /**
     * The response message for [GetAllFilesRequest], representing a single page of `.rs` schema files.
     *
     * This response contains a list of [RSFile]s. If not all files are returned in this chunk,
     * a non-null [pageToken] is returned and must be passed in the next request.
     *
     * This is useful for large metadata sets where streaming or full download isn't viable.
     *
     * @property files The chunk of schema files returned.
     * @property pageToken A continuation token for pagination. Null if there are no more files to fetch.
     */
    @Serializable
    public class Response private constructor(
        @ProtoNumber(1)
        public val files: List<RSFile> = emptyList(),

        @ProtoNumber(2)
        public val pageToken: String?,
    ) : RSProtoType {

        override val definition: RSProtoType.Definition<*>
            get() = Companion

        public companion object : RSProtoType.Definition<Response> {
            override val url: String = "type.googleapis.com/app.timemate.metadata.response.GetAllFiles"
            override val Default: Response = Response(pageToken = null)

            /**
             * Creates a [Response] using a DSL-style builder block.
             *
             * This approach helps construct structured chunked responses easily.
             *
             * Example:
             * ```
             * val response = GetAllFilesRequest.Response.create {
             *     files = listOf(file1, file2)
             *     pageToken = "next-page"
             * }
             * ```
             *
             * @param builder Lambda to configure response properties.
             * @return the constructed [Response]
             */
            public fun create(builder: Builder.() -> Unit): Response {
                return Builder().apply(builder).build()
            }

            /**
             * DSL-style builder for [Response].
             */
            public class Builder {
                @JvmField
                public var files: List<RSFile> = emptyList()
                @JvmField
                public var pageToken: String? = null

                public fun build(): Response = Response(
                    files = files,
                    pageToken = pageToken
                )
            }
        }
    }
}
