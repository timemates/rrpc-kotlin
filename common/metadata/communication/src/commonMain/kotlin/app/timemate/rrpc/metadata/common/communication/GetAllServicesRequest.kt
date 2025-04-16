package app.timemate.rrpc.metadata.common.communication

import app.timemate.rrpc.RSProtoType
import app.timemate.rrpc.proto.schema.RSService
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@Serializable
public class GetAllServicesRequest private constructor(
    /**
     * Maximum number of services to return in a single page.
     * If `0`, the backend chooses an optimal size.
     */
    @ProtoNumber(1)
    public val pageSize: Int = 0,

    /**
     * Continuation token from the previous page, or null to begin from the start.
     */
    @ProtoNumber(2)
    public val pageToken: String?,
) : RSProtoType {

    override val definition: RSProtoType.Definition<*>
        get() = Companion

    public companion object : RSProtoType.Definition<GetAllServicesRequest> {
        override val url: String = "type.googleapis.com/app.timemate.metadata.request.GetAllServices"

        /**
         * A default empty request with no paging configuration.
         */
        override val Default: GetAllServicesRequest = GetAllServicesRequest(pageSize = 0, pageToken = null)

        /**
         * DSL-style factory to build [GetAllServicesRequest].
         *
         * Example:
         * ```
         * val request = GetAllServicesRequest.create {
         *     pageSize = 20
         *     pageToken = "next-page"
         * }
         * ```
         */
        public fun create(builder: Builder.() -> Unit): GetAllServicesRequest {
            return Builder().apply(builder).build()
        }

        public class Builder {
            public var pageSize: Int = 0
            public var pageToken: String? = null

            public fun build(): GetAllServicesRequest =
                GetAllServicesRequest(pageSize = pageSize, pageToken = pageToken)
        }
    }

    /**
     * Response message for [GetAllServicesRequest].
     *
     * Services are returned in paged chunks to allow scalable discovery,
     * especially when dealing with large distributed metadata registries.
     */
    @Serializable
    public class Response private constructor(
        /**
         * The services discovered for this page.
         */
        @ProtoNumber(1)
        public val services: List<RSService>,

        /**
         * Pagination token for the next chunk, or null if this is the final page.
         */
        @ProtoNumber(2)
        public val pageToken: String?,
    ) : RSProtoType {

        override val definition: RSProtoType.Definition<*>
            get() = Companion

        public companion object : RSProtoType.Definition<Response> {
            override val url: String = "type.googleapis.com/app.timemate.metadata.response.GetAllServices"
            override val Default: Response = Response(services = emptyList(), pageToken = null)

            /**
             * DSL-style factory to build [Response].
             *
             * Example:
             * ```
             * val response = GetAllServicesRequest.Response.create {
             *     services = listOf(service1, service2)
             *     pageToken = "next"
             * }
             * ```
             */
            public fun create(builder: Builder.() -> Unit): Response {
                return Builder().apply(builder).build()
            }

            public class Builder {
                public var services: List<RSService> = emptyList()
                public var pageToken: String? = null

                public fun build(): Response = Response(services = services, pageToken = pageToken)
            }
        }
    }
}
