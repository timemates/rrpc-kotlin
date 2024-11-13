package org.timemates.rrpc.codegen.typemodel

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName

internal object LibClassNames {
    fun Flow(ofTypeName: TypeName): ParameterizedTypeName =
        ClassName("kotlinx.coroutines.flow", "Flow")
            .parameterizedBy(ofTypeName)

    val ServiceDescriptor = ClassName("org.timemates.rrpc.server.module.descriptors", "ServiceDescriptor")

    val ByteReadPacket = ClassName("io.ktor.utils.io.core", "ByteReadPacket")

    val Payload = ClassName("io.rsocket.kotlin.payload", "Payload")

    object ProcedureDescriptor {
        val base = ClassName(
            "org.timemates.rrpc.server.module.descriptors", "ProcedureDescriptor"
        )

        val requestResponse = base.nestedClass("RequestResponse")

        val requestStream = base.nestedClass("RequestStream")

        val requestChannel = base.nestedClass("RequestChannel")
    }

    object Option {
        val Base = ClassName("org.timemates.rrpc.options", "Option")
        val File = ClassName("org.timemates.rrpc.options", "FileOption")
        val Service = ClassName("org.timemates.rrpc.options", "ServiceOption")
        val RPC = ClassName("org.timemates.rrpc.options", "RPCOption")
    }

    val Options = ClassName("org.timemates.rrpc.options", "Options")

    val ClientMetadata = ClassName("org.timemates.rrpc.metadata", "ClientMetadata")

    val ExtraMetadata = ClassName("org.timemates.rrpc.metadata", "ExtraMetadata")

    val ProtoBuf = ClassName("kotlinx.serialization.protobuf", "ProtoBuf")

    val RSocket = ClassName("io.rsocket.kotlin", "RSocket")

    val RRpcClientConfig = ClassName("org.timemates.rrpc.client.config", "RRpcClientConfig")

    val RRpcServerService = ClassName("org.timemates.rrpc.server.module", "RRpcService")

    val RRpcClientService = ClassName("org.timemates.rrpc.client", "RRpcServiceClient")

    val ExperimentalSerializationApi = ClassName("kotlinx.serialization", "ExperimentalSerializationApi")

    val KSerializer = ClassName("kotlinx.serialization", "KSerializer")

    val Interceptors = ClassName("org.timemates.rrpc.interceptors", "Interceptors")

    val RequestContext = ClassName("org.timemates.rrpc.server", "RequestContext")

    val ProtoType = ClassName("org.timemates.rrpc", "ProtoType")

    fun ProtoTypeDefinition(type: TypeName) = ProtoType.nestedClass("Definition")
            .parameterizedBy(type)

    object RS {
        val File = ClassName("org.timemates.rrpc.common.metadata", "RSFile")
        val Constant = ClassName("org.timemates.rrpc.common.metadata", "RSEnumConstant")
        val Extend = ClassName("org.timemates.rrpc.common.metadata", "RSExtend")
        val Field = ClassName("org.timemates.rrpc.common.metadata", "RSField")
        val OneOf = ClassName("org.timemates.rrpc.common.metadata", "RSOneOf")
        val Option = ClassName("org.timemates.rrpc.common.metadata", "RSOption")
        val OptionValueRaw = Option.nestedClass("Value").nestedClass("Raw")
        val OptionValueRawMap = Option.nestedClass("Value").nestedClass("RawMap")
        val OptionValueMessageMap = Option.nestedClass("Value").nestedClass("MessageMap")
        val Options = ClassName("org.timemates.rrpc.common.metadata", "RSOptions")
        val Rpc = ClassName("org.timemates.rrpc.common.metadata", "RSRpc")
        val Message = ClassName("org.timemates.rrpc.common.metadata", "RSType", "Message")
        val Enum = ClassName("org.timemates.rrpc.common.metadata", "RSType", "Enum")
        val Enclosing = ClassName("org.timemates.rrpc.common.metadata", "RSType", "Enclosing")

        val Service = ClassName("org.timemates.rrpc.common.metadata", "RSService")

        val TypeMemberUrl = ClassName("org.timemates.rrpc.common.metadata", "RSTypeMemberUrl")
        val StreamableTypeUrl = ClassName("org.timemates.rrpc.common.metadata", "StreamableRSTypeUrl")

        object Value {
            val PackageName = ClassName("org.timemates.rrpc.common.metadata.value", "RSPackageName")
            val TypeUrl = ClassName("org.timemates.rrpc.common.metadata.value", "RSTypeUrl")
        }

        val Resolver = ClassName("org.timemates.rrpc.common.metadata", "RSResolver")

        object Server {
            val MetadataLookup = ClassName("org.timemates.rrpc.server.metadata", "MetadataLookup")
        }
    }

    object Wrappers {
        val INT32_VALUE = ClassName("com.google.protobuf", "Int32Wrapper")
        val INT64_VALUE = ClassName("com.google.protobuf", "Int64Wrapper")
        val UINT32_VALUE = ClassName("com.google.protobuf", "UInt32Wrapper")
        val UINT64_VALUE = ClassName("com.google.protobuf", "UInt64Wrapper")
        val FLOAT_VALUE = ClassName("com.google.protobuf", "FloatWrapper")
        val DOUBLE_VALUE = ClassName("com.google.protobuf", "DoubleWrapper")
        val BOOL_VALUE = ClassName("com.google.protobuf", "BoolWrapper")
        val BYTES_VALUE = ClassName("com.google.protobuf", "BytesWrapper")
        val STRING_VALUE = ClassName("com.google.protobuf", "StringWrapper")
    }
}