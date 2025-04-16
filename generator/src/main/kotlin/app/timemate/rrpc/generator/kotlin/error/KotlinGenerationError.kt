package app.timemate.rrpc.generator.kotlin.error

import app.timemate.rrpc.generator.plugin.api.result.ProcessingError
import app.timemate.rrpc.proto.schema.RSElementLocation
import app.timemate.rrpc.proto.schema.RSExtend
import app.timemate.rrpc.proto.schema.RSFile
import app.timemate.rrpc.proto.schema.RSRpc
import app.timemate.rrpc.proto.schema.RSTypeMemberUrl
import app.timemate.rrpc.proto.schema.value.RSDeclarationUrl

public sealed interface KotlinGenerationError : ProcessingError

public data class UnresolvableDeclarationError(
    public val declaration: RSDeclarationUrl,
) : KotlinGenerationError {
    override val message: String =
        """
            |Unable to resolve declaration '${declaration.simpleName}' at ${declaration.enclosingTypeOrPackage}. 
            |It's most likely internal error that should be reported.
        """.trimMargin()
}

public data class UnresolvableDeclarationMemberError(
    public val declaration: RSTypeMemberUrl,
) : KotlinGenerationError {
    override val message: String =
        """
            |Unable to resolve member '${declaration.memberName}' of declaration '${declaration.typeUrl}'. 
            |It's most likely internal error that should be reported.
        """.trimMargin()
}

public data class ClientOnlyStreamingRpcError(
    public val rpc: RSRpc,
) : KotlinGenerationError {
    override val message: String =
        "Client only streaming is not supported, but defined at ${rpc.location} for '${rpc.name}."
}

public data class Proto2FilesAreNotSupportedError(
    public val file: RSFile,
) : KotlinGenerationError {
    override val message: String = "Syntax of file at '${file.location}' uses proto2, but the required is proto3."
}

public data class AckTypeCannotBeStreamingError(
    public val rpc: RSRpc,
) : KotlinGenerationError {
    override val message: String =
        "`timemates.rrpc.Ack` cannot be used as streaming type, but used in file '${rpc.location}' for '${rpc.name}'."
}

public data class ProtoAnyCannotBeOptionError(
    public val file: RSFile,
) : KotlinGenerationError {
    override val message: String =
        "google.protobuf.Any type is not supported as an option type, but used in ${file.location}."
}

public data class ExtendingMessagesAreNotSupportedError(
    public val extend: RSExtend
) : KotlinGenerationError {
    override val message: String = "Extending messages are not supported, but extended at ${extend.location}."
}

public data class FileCannotBeResolvedError(
    public val location: RSElementLocation,
) : KotlinGenerationError {
    override val message: String = "File '${location}' cannot be resolved."
}

public data class UnsupportedTypeError(
    public val type: RSDeclarationUrl,
) : KotlinGenerationError {
    override val message: String = "Type '${type}' is not supported by the generator."
}

public data class UnsupportedTypeForSerializationError(
    public val typeUrl: RSDeclarationUrl,
) : KotlinGenerationError {
    override val message: String = "Type '${typeUrl}' is not supported by the kotlinx.serialization.protobuf, therefore cannot be used in the generation."
}