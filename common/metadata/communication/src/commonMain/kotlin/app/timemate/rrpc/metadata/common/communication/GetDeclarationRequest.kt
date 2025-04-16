package app.timemate.rrpc.metadata.common.communication

import app.timemate.rrpc.RSProtoType
import app.timemate.rrpc.proto.schema.RSEnum
import app.timemate.rrpc.proto.schema.RSMessage
import app.timemate.rrpc.proto.schema.RSNode
import app.timemate.rrpc.proto.schema.RSService
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@Serializable
public class GetDeclarationRequest private constructor(
    /**
     * A list of type URLs to retrieve declarations for.
     *
     * Each URL should correspond to a valid `RSNode` (message, enum, extend).
     * Invalid or unresolved URLs will simply not be present in the response.
     */
    @ProtoNumber(1)
    public val urls: List<String>,
) : RSProtoType {

    override val definition: RSProtoType.Definition<*>
        get() = Companion

    public companion object : RSProtoType.Definition<GetDeclarationRequest> {
        override val url: String = "type.googleapis.com/app.timemate.metadata.request.GetDeclaration"
        override val Default: GetDeclarationRequest = GetDeclarationRequest(urls = emptyList())

        /**
         * DSL-style factory for creating a [GetDeclarationRequest].
         *
         * Example:
         * ```
         * val request = GetDeclarationRequest.create {
         *     urls = listOf("type.googleapis.com/my.Service.Message", "type.googleapis.com/my.Enum")
         * }
         * ```
         */
        public fun create(builder: Builder.() -> Unit): GetDeclarationRequest {
            return Builder().apply(builder).build()
        }

        public class Builder {
            public var urls: List<String> = emptyList()

            public fun build(): GetDeclarationRequest = GetDeclarationRequest(urls)
        }
    }

    /**
     * Response message for [GetDeclarationRequest], containing only successfully resolved declarations.
     *
     * Missing or unresolved URLs are silently skipped, making this response fault-tolerant.
     */
    @Serializable
    public data class Response(
        /**
         * Successfully resolved declarations indexed by their corresponding type URL.
         * Each node can be a message, enum, or extend.
         */
        @ProtoNumber(1)
        val declarations: Map<String, Result>,
    ) : RSProtoType {

        override val definition: RSProtoType.Definition<*>
            get() = Companion

        public companion object : RSProtoType.Definition<Response> {
            override val url: String = "type.googleapis.com/app.timemate.metadata.response.GetDeclaration"
            override val Default: Response = Response(declarations = emptyMap())

            /**
             * DSL-style factory for creating a [Response].
             *
             * Example:
             * ```
             * val response = GetDeclarationRequest.Response.create {
             *     declarations = mapOf("type.googleapis.com/Foo" to fooNode)
             * }
             * ```
             */
            public fun create(builder: Builder.() -> Unit): Response {
                return Builder().apply(builder).build()
            }

            public class Builder {
                public var declarations: Map<String, Result> = emptyMap()

                public fun build(): Response = Response(declarations)
            }
        }

        /**
         * Represents the result of a metadata declaration query for a specific [RSNode].
         *
         * This class is used to wrap the result of the [GetDeclarationRequest] in cases where the declaration is polymorphic
         * (i.e., it could be an [RSService], [RSMessage], or [RSEnum]), since these types cannot be directly represented
         * as a single field in Protobuf. The class holds one of these types based on the declaration's nature.
         * If the declaration is unresolved, all fields will be `null`.
         *
         * @param service The service node, if the declaration is an [RSService].
         * @param message The message node, if the declaration is an [RSMessage].
         * @param enum The enum node, if the declaration is an [RSEnum].
         *
         * This class is used internally to allow flexible handling of metadata declarations while ensuring compatibility
         * with Protobuf's limitations for polymorphic types.
         */
        @Serializable
        public class Result private constructor(
            /**
             * The service representation of the declaration, if it is an [RSService].
             */
            @ProtoNumber(1)
            public val service: RSService?,

            /**
             * The message representation of the declaration, if it is an [RSMessage].
             */
            @ProtoNumber(2)
            public val message: RSMessage?,

            /**
             * The enum representation of the declaration, if it is an [RSEnum].
             */
            @ProtoNumber(3)
            public val enum: RSEnum?,
        ) : RSProtoType {
            public val node: RSNode? get() = service ?: message ?: enum

            /**
             * Secondary constructor that creates a [Result] from an [RSNode]. This allows wrapping any of the
             * polymorphic types into a [Result].
             *
             * @param node The [RSNode] to wrap. It can be an [RSService], [RSMessage], or [RSEnum].
             */
            public constructor(node: RSNode) : this(
                service = node as? RSService,
                message = node as? RSMessage,
                enum = node as? RSEnum,
            )

            /**
             * Constructor to create a [Result] containing only an [RSService] declaration.
             *
             * If the declaration is specifically an [RSService], this constructor can be used to create a [Result]
             * with the [RSService] populated and other fields set to `null`.
             *
             * @param service The [RSService] to be wrapped in the result.
             */
            public constructor(service: RSService) : this(
                service = service,
                message = null,
                enum = null,
            )

            /**
             * Constructor to create a [Result] containing only an [RSMessage] declaration.
             *
             * If the declaration is specifically an [RSMessage], this constructor can be used to create a [Result]
             * with the [RSMessage] populated and other fields set to `null`.
             *
             * @param message The [RSMessage] to be wrapped in the result.
             */
            public constructor(message: RSMessage) : this(
                service = null,
                message = message,
                enum = null,
            )

            /**
             * Constructor to create a [Result] containing only an [RSEnum] declaration.
             *
             * If the declaration is specifically an [RSEnum], this constructor can be used to create a [Result]
             * with the [RSEnum] populated and other fields set to `null`.
             *
             * @param enum The [RSEnum] to be wrapped in the result.
             */
            public constructor(enum: RSEnum) : this(
                service = null,
                message = null,
                enum = enum,
            )

            /**
             * The definition of this ProtoType, used for serialization and deserialization.
             */
            public override val definition: RSProtoType.Definition<*> get() = Companion

            /**
             * Companion object providing metadata about the [Result] type.
             */
            public companion object : RSProtoType.Definition<Result> {

                /**
                 * The URL identifying the Protobuf type for this [Result] in the schema.
                 */
                override val url: String = "type.googleapis.com/app.timemate.metadata.response.GetDeclaration.Result"

                /**
                 * The default instance of [Result], with all fields set to `null` to represent an unresolved declaration.
                 */
                override val Default: Result = Result(null, null, null)
            }
        }

    }
}
