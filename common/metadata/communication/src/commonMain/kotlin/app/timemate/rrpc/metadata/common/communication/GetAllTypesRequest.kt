package app.timemate.rrpc.metadata.common.communication

import app.timemate.rrpc.RSProtoType
import app.timemate.rrpc.proto.schema.RSEnum
import app.timemate.rrpc.proto.schema.RSMessage
import app.timemate.rrpc.proto.schema.RSType
import kotlinx.serialization.protobuf.ProtoNumber
import kotlinx.serialization.*

@Serializable
public class GetAllTypesRequest private constructor(
    /**
     * The number of types to return in a single response page.
     * If set to `0`, the server decides the appropriate chunk size.
     */
    @ProtoNumber(1)
    public val pageSize: Int = 0,

    /**
     * A pagination token used to retrieve the next chunk of results.
     * Initially null. Set to the value returned in the previous response to continue.
     */
    @ProtoNumber(2)
    public val pageToken: String?,
) : RSProtoType {

    override val definition: RSProtoType.Definition<*>
        get() = Companion

    public companion object : RSProtoType.Definition<GetAllTypesRequest> {
        override val url: String = "type.googleapis.com/app.timemate.metadata.request.GetAllTypes"
        override val Default: GetAllTypesRequest = GetAllTypesRequest(pageToken = null)

        /**
         * DSL-style factory for building a [GetAllTypesRequest].
         *
         * Example:
         * ```
         * val request = GetAllTypesRequest.create {
         *     pageSize = 100
         *     pageToken = "abc123"
         * }
         * ```
         */
        public fun create(builder: Builder.() -> Unit): GetAllTypesRequest {
            return Builder().apply(builder).build()
        }

        public class Builder {
            public var pageSize: Int = 0
            public var pageToken: String? = null

            public fun build(): GetAllTypesRequest = GetAllTypesRequest(
                pageSize = pageSize,
                pageToken = pageToken
            )
        }
    }

    /**
     * Response message for [GetAllTypesRequest], representing a chunk of declared types.
     *
     * Includes all types: messages, enums, and extensions, each represented as an [RSType].
     * Types are returned in a paginated fashion to avoid overloading clients with large payloads.
     */
    @Serializable
    public class Response private constructor(
        /**
         * The list of resolved types in this chunk.
         */
        @ProtoNumber(1)
        public val messages: List<RSMessage> = emptyList(),

        /**
         * The list of resolved enums
         */
        @ProtoNumber(2)
        public val enums: List<RSEnum> = emptyList(),

        /**
         * A continuation token. Null if this is the last chunk.
         */
        @ProtoNumber(3)
        public val pageToken: String?,
    ) : RSProtoType {

        @Transient
        public val types: List<RSType> = messages + enums

        override val definition: RSProtoType.Definition<*>
            get() = Companion

        public companion object : RSProtoType.Definition<Response> {
            override val url: String = "type.googleapis.com/app.timemate.metadata.response.GetAllTypes"
            override val Default: Response = Response(pageToken = null)

            /**
             * DSL-style factory for building a [Response].
             *
             * Example:
             * ```
             * val response = GetAllTypesRequest.Response.create {
             *     types = listOf(type1, type2)
             *     pageToken = "nextPageToken"
             * }
             * ```
             */
            public fun create(builder: Builder.() -> Unit): Response {
                return Builder().apply(builder).build()
            }

            public class Builder {
                public var types: List<RSType> = emptyList()
                public var pageToken: String? = null

                public fun build(): Response = Response(
                    types.filterIsInstance<RSMessage>(),
                    types.filterIsInstance<RSEnum>(),
                    pageToken,
                )
            }
        }
    }
}
