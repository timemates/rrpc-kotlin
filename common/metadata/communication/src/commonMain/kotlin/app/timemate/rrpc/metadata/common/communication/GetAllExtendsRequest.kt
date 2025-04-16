package app.timemate.rrpc.metadata.common.communication

import app.timemate.rrpc.RSProtoType
import app.timemate.rrpc.proto.schema.RSExtend
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@Serializable
public class GetAllExtendsRequest private constructor(
    /**
     * Number of extension types to include in this page.
     * Set to `0` to let the backend decide the optimal page size.
     */
    @ProtoNumber(1)
    public val pageSize: Int = 0,

    /**
     * Continuation token from the previous response.
     * Set to null to start from the beginning.
     */
    @ProtoNumber(2)
    public val pageToken: String?,
) : RSProtoType {

    override val definition: RSProtoType.Definition<*>
        get() = Companion

    public companion object : RSProtoType.Definition<GetAllExtendsRequest> {
        override val url: String = "type.googleapis.com/app.timemate.metadata.request.GetAllExtends"

        /**
         * Default request with no paging.
         */
        override val Default: GetAllExtendsRequest = GetAllExtendsRequest(pageToken = null)

        /**
         * DSL-style builder to construct [GetAllExtendsRequest].
         *
         * Example:
         * ```
         * val request = GetAllExtendsRequest.create {
         *     pageSize = 100
         *     pageToken = "abc123"
         * }
         * ```
         */
        public fun create(builder: Builder.() -> Unit): GetAllExtendsRequest {
            return Builder().apply(builder).build()
        }

        public class Builder {
            public var pageSize: Int = 0
            public var pageToken: String? = null

            public fun build(): GetAllExtendsRequest =
                GetAllExtendsRequest(pageSize = pageSize, pageToken = pageToken)
        }
    }

    /**
     * Response message for [GetAllExtendsRequest].
     *
     * Contains a paged list of `RSExtend` types (aka "extension declarations").
     */
    @Serializable
    public class Response private constructor(
        /**
         * The list of extension declarations returned in this chunk.
         */
        @ProtoNumber(1)
        public val extends: List<RSExtend>,

        /**
         * Continuation token for retrieving the next page.
         * Will be null if there are no further pages.
         */
        @ProtoNumber(2)
        public val pageToken: String?,
    ) : RSProtoType {

        override val definition: RSProtoType.Definition<*>
            get() = Companion

        public companion object : RSProtoType.Definition<Response> {
            override val url: String = "type.googleapis.com/app.timemate.metadata.response.GetAllExtends"
            override val Default: Response = Response(extends = emptyList(), pageToken = null)

            /**
             * DSL-style builder to construct a [Response].
             *
             * Example:
             * ```
             * val response = GetAllExtendsRequest.Response.create {
             *     extends = listOf(ext1, ext2)
             *     pageToken = "nextPageToken"
             * }
             * ```
             */
            public fun create(builder: Builder.() -> Unit): Response {
                return Builder().apply(builder).build()
            }

            public class Builder {
                public var extends: List<RSExtend> = emptyList()
                public var pageToken: String? = null

                public fun build(): Response = Response(extends = extends, pageToken = pageToken)
            }
        }
    }
}
