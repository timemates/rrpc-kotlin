package app.timemate.rrpc.generator.kotlin.internal

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName

internal object LibClassNames {
    fun Flow(ofTypeName: TypeName): ParameterizedTypeName =
        ClassName("kotlinx.coroutines.flow", "Flow")
            .parameterizedBy(ofTypeName)

    val ServiceDescriptor = ClassName("app.timemate.rrpc.server.module.descriptors", "ServiceDescriptor")

    val ByteReadPacket = ClassName("io.ktor.utils.io.core", "ByteReadPacket")

    val Payload = ClassName("io.rsocket.kotlin.payload", "Payload")

    object ProcedureDescriptor {
        val base = ClassName(
            "app.timemate.rrpc.server.module.descriptors", "ProcedureDescriptor"
        )

        val requestResponse = base.nestedClass("RequestResponse")

        val requestStream = base.nestedClass("RequestStream")

        val requestChannel = base.nestedClass("RequestChannel")

        val fireAndForget = base.nestedClass("FireAndForget")

        val metadataPush = base.nestedClass("MetadataPush")
    }

    object Option {
        val Base = ClassName("app.timemate.rrpc.options", "Option")
        val File = ClassName("app.timemate.rrpc.options", "FileOption")
        val Service = ClassName("app.timemate.rrpc.options", "ServiceOption")
        val RPC = ClassName("app.timemate.rrpc.options", "RPCOption")
    }

    val RPCsOptions = ClassName("app.timemate.rrpc.client.options", "RPCsOptions")
    val OptionsWithValue = ClassName("app.timemate.rrpc.options", "OptionsWithValue")

    val ClientMetadata = ClassName("app.timemate.rrpc.metadata", "ClientMetadata")

    val ExtraMetadata = ClassName("app.timemate.rrpc.metadata", "ExtraMetadata")

    val ProtoBuf = ClassName("kotlinx.serialization.protobuf", "ProtoBuf")

    val RSocket = ClassName("io.rsocket.kotlin", "RSocket")

    val RRpcClientConfig = ClassName("app.timemate.rrpc.client.config", "RRpcClientConfig")

    val RRpcServerService = ClassName("app.timemate.rrpc.server.module", "RRpcService")

    val RRpcClientService = ClassName("app.timemate.rrpc.client", "RRpcServiceClient")

    val ExperimentalSerializationApi = ClassName("kotlinx.serialization", "ExperimentalSerializationApi")

    val KSerializer = ClassName("kotlinx.serialization", "KSerializer")

    val Interceptors = ClassName("app.timemate.rrpc.interceptors", "Interceptors")

    val RequestContext = ClassName("app.timemate.rrpc.server", "RequestContext")

    val ProtoType = ClassName("app.timemate.rrpc", "ProtoType")

    fun ProtoTypeDefinition(type: TypeName) = ProtoType.nestedClass("Definition")
            .parameterizedBy(type)

    object RS {
        val File = ClassName("app.timemate.rrpc.common.metadata", "RSFile")
        val Constant = ClassName("app.timemate.rrpc.common.metadata", "RSEnumConstant")
        val Extend = ClassName("app.timemate.rrpc.common.metadata", "RSExtend")
        val Field = ClassName("app.timemate.rrpc.common.metadata", "RSField")
        val OneOf = ClassName("app.timemate.rrpc.common.metadata", "RSOneOf")
        val Option = ClassName("app.timemate.rrpc.common.metadata", "RSOption")
        val OptionValueRaw = Option.nestedClass("Value").nestedClass("Raw")
        val OptionValueRawMap = Option.nestedClass("Value").nestedClass("RawMap")
        val OptionValueMessageMap = Option.nestedClass("Value").nestedClass("MessageMap")
        val Options = ClassName("app.timemate.rrpc.common.metadata", "RSOptions")
        val Rpc = ClassName("app.timemate.rrpc.common.metadata", "RSRpc")
        val Message = ClassName("app.timemate.rrpc.common.metadata", "RSType", "Message")
        val Enum = ClassName("app.timemate.rrpc.common.metadata", "RSType", "Enum")
        val Enclosing = ClassName("app.timemate.rrpc.common.metadata", "RSType", "Enclosing")

        val Service = ClassName("app.timemate.rrpc.common.metadata", "RSService")

        val TypeMemberUrl = ClassName("app.timemate.rrpc.common.metadata", "RSTypeMemberUrl")
        val StreamableTypeUrl = ClassName("app.timemate.rrpc.common.metadata", "StreamableRSTypeUrl")

        object Value {
            val PackageName = ClassName("app.timemate.rrpc.common.metadata.value", "RSPackageName")
            val TypeUrl = ClassName("app.timemate.rrpc.common.metadata.value", "RSTypeUrl")
        }

        val Resolver = ClassName("app.timemate.rrpc.common.metadata", "RSResolver")

        object Server {
            val MetadataLookup = ClassName("app.timemate.rrpc.server.metadata", "MetadataLookup")
        }
    }

    object Wrappers {
        val INT32_VALUE = ClassName("com.google.protobuf", "ProtoInt32Wrapper")
        val INT64_VALUE = ClassName("com.google.protobuf", "ProtoInt64Wrapper")
        val UINT32_VALUE = ClassName("com.google.protobuf", "ProtoUInt32Wrapper")
        val UINT64_VALUE = ClassName("com.google.protobuf", "ProtoUInt64Wrapper")
        val FLOAT_VALUE = ClassName("com.google.protobuf", "ProtoFloatWrapper")
        val DOUBLE_VALUE = ClassName("com.google.protobuf", "ProtoDoubleWrapper")
        val BOOL_VALUE = ClassName("com.google.protobuf", "ProtoBoolWrapper")
        val BYTES_VALUE = ClassName("com.google.protobuf", "ProtoBytesWrapper")
        val STRING_VALUE = ClassName("com.google.protobuf", "ProtoStringWrapper")
    }
}