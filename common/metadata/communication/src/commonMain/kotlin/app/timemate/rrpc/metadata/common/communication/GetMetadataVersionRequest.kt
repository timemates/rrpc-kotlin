package app.timemate.rrpc.metadata.common.communication

import app.timemate.rrpc.RSProtoType
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@Serializable
public class GetMetadataVersionRequest private constructor(
    /**
     * Pagination token used for fetching the next set of metadata versions.
     * Null if starting from the first.
     */
    @ProtoNumber(1)
    public val pageToken: String?,
) : RSProtoType {

    override val definition: RSProtoType.Definition<*>
        get() = Companion

    public companion object : RSProtoType.Definition<GetMetadataVersionRequest> {
        override val url: String = "type.googleapis.com/app.timemate.metadata.request.GetMetadataVersion"

        /**
         * Default request, starting from the first page.
         */
        override val Default: GetMetadataVersionRequest = GetMetadataVersionRequest(pageToken = null)

        /**
         * DSL-style builder to construct [GetMetadataVersionRequest].
         *
         * Example:
         * ```
         * val request = GetMetadataVersionRequest.create {
         *     pageToken = "abc123"
         * }
         * ```
         */
        public fun create(builder: Builder.() -> Unit): GetMetadataVersionRequest {
            return Builder().apply(builder).build()
        }

        public class Builder {
            public var pageToken: String? = null

            public fun build(): GetMetadataVersionRequest =
                GetMetadataVersionRequest(pageToken = pageToken)
        }
    }

    /**
     * Response message for [GetMetadataVersionRequest].
     *
     * Provides the version information, including the current version and the minimum supported version.
     */
    @Serializable
    public class Response private constructor(
        /**
         * The current version of the metadata service.
         */
        @ProtoNumber(1)
        public val currentVersion: Double,

        /**
         * The minimum supported version for backward compatibility checks.
         */
        @ProtoNumber(2)
        public val minimumSupportedVersion: Double,

        /**
         * A continuation token, used to fetch subsequent pages of metadata version history.
         * Null if this is the last chunk.
         */
        @ProtoNumber(3)
        public val pageToken: String?,
    ) : RSProtoType {

        override val definition: RSProtoType.Definition<*>
            get() = Companion

        public companion object : RSProtoType.Definition<Response> {
            override val url: String = "type.googleapis.com/app.timemate.metadata.response.GetMetadataVersion"
            override val Default: Response = Response(currentVersion = 1.0, minimumSupportedVersion = 1.0, pageToken = null)

            /**
             * DSL-style builder to construct a [Response].
             *
             * Example:
             * ```
             * val response = GetMetadataVersionRequest.Response.create {
             *     currentVersion = 2.1
             *     minimumSupportedVersion = 1.5
             *     pageToken = "nextPage"
             * }
             * ```
             */
            public fun create(builder: Builder.() -> Unit): Response {
                return Builder().apply(builder).build()
            }

            public class Builder {
                public var currentVersion: Double = 1.0
                public var minimumSupportedVersion: Double = 1.0
                public var pageToken: String? = null

                public fun build(): Response = Response(
                    currentVersion = currentVersion,
                    minimumSupportedVersion = minimumSupportedVersion,
                    pageToken = pageToken
                )
            }
        }
    }
}
