package org.timemates.rrpc.generator.kotlin.dyser

import com.google.protobuf.*
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.protobuf.ProtoNumber
import org.timemates.rrpc.common.schema.RSResolver
import org.timemates.rrpc.common.schema.RSType
import org.timemates.rrpc.common.schema.value.RMDeclarationUrl

public class MessageSerializerFactory(
    private val resolver: RSResolver,
) {
    private val descriptorsCache: MutableMap<RMDeclarationUrl, SerialDescriptor> = mutableMapOf()
    private val serializersCache: MutableMap<RMDeclarationUrl, KSerializer<*>> = mutableMapOf()

    public fun getMessageSerializer(message: RSType.Message): KSerializer<MessageValues> {
        return serializersCache.getOrPut(message.typeUrl) {
            MessageValuesSerializer(message, this, resolver, getOrCreateMessageDescriptor(message))
        } as MessageValuesSerializer
    }

    public fun getDescriptor(type: RSType): SerialDescriptor {
        return when (type) {
            is RSType.Enclosing -> ProtoEmpty.serializer().descriptor
            is RSType.Enum -> getOrCreateEnumDescriptor(type)
            is RSType.Message -> getOrCreateMessageDescriptor(type)
        }
    }

    @OptIn(InternalSerializationApi::class)
    private fun getOrCreateEnumDescriptor(enum: RSType.Enum): SerialDescriptor {
        return descriptorsCache.getOrPut(enum.typeUrl) {
            buildSerialDescriptor(enum.name, SerialKind.ENUM) {
                enum.constants.forEach {
                    element(
                        elementName = it.name,
                        descriptor = buildSerialDescriptor(it.name, StructureKind.OBJECT),
                        annotations = listOf(ProtoNumber(it.tag)),
                    )
                }
            }
        }
    }

    @OptIn(InternalSerializationApi::class)
    private fun getOrCreateMessageDescriptor(message: RSType.Message): SerialDescriptor {
        // Check if the descriptor is already cached
        descriptorsCache[message.typeUrl]?.let { return it }

        // Create a mutable proxy descriptor and cache it to handle recursion
        val proxyDescriptor = SerialDescriptorProxy()
        descriptorsCache[message.typeUrl] = proxyDescriptor

        proxyDescriptor.descriptor = buildClassSerialDescriptor(message.name) {
            message.allFields.asSequence().sortedBy { it.tag }.map { field ->
                field to when (field.typeUrl) {
                    RMDeclarationUrl.STRING -> String.serializer().descriptor
                    RMDeclarationUrl.BYTES -> ByteArraySerializer().descriptor
                    RMDeclarationUrl.INT32 -> Int.serializer().descriptor
                    RMDeclarationUrl.INT64 -> Long.serializer().descriptor
                    RMDeclarationUrl.BOOL -> Boolean.serializer().descriptor
                    RMDeclarationUrl.DOUBLE -> Double.serializer().descriptor
                    RMDeclarationUrl.FLOAT -> Float.serializer().descriptor
                    RMDeclarationUrl.UINT32 -> UInt.serializer().descriptor
                    RMDeclarationUrl.UINT64 -> ULong.serializer().descriptor
                    RMDeclarationUrl.INT32_VALUE -> ProtoInt32Wrapper.serializer().descriptor
                    RMDeclarationUrl.INT64_VALUE -> ProtoInt64Wrapper.serializer().descriptor
                    RMDeclarationUrl.UINT32_VALUE -> ProtoUInt32Wrapper.serializer().descriptor
                    RMDeclarationUrl.UINT64_VALUE -> ProtoUInt64Wrapper.serializer().descriptor
                    RMDeclarationUrl.BOOL_VALUE -> ProtoBoolWrapper.serializer().descriptor
                    RMDeclarationUrl.FLOAT_VALUE -> ProtoFloatWrapper.serializer().descriptor
                    RMDeclarationUrl.DOUBLE_VALUE -> ProtoDoubleWrapper.serializer().descriptor
                    RMDeclarationUrl.STRING_VALUE -> ProtoStringWrapper.serializer().descriptor
                    RMDeclarationUrl.TIMESTAMP -> ProtoTimestamp.serializer().descriptor
                    RMDeclarationUrl.DURATION -> ProtoDuration.serializer().descriptor
                    RMDeclarationUrl.ANY -> ProtoAny.serializer().descriptor
                    RMDeclarationUrl.STRUCT, RMDeclarationUrl.STRUCT_MAP -> ProtoStruct.serializer().descriptor
                    RMDeclarationUrl.STRUCT_VALUE -> ProtoStructValue.serializer().descriptor
                    RMDeclarationUrl.STRUCT_LIST -> ProtoStructValueKind.ListValue.serializer().descriptor
                    RMDeclarationUrl.STRUCT_NULL -> ProtoStructValueKind.NullValue.serializer().descriptor
                    RMDeclarationUrl.ACK, RMDeclarationUrl.EMPTY -> ProtoEmpty.serializer().descriptor
                    else -> {
                        when (val type = resolver.resolveType(field.typeUrl)!!) {
                            is RSType.Enclosing -> ProtoEmpty.serializer().descriptor
                            is RSType.Message -> getOrCreateMessageDescriptor(type)
                            is RSType.Enum -> getOrCreateEnumDescriptor(type)
                        }

                    }
                }
            }.forEach { (field, descriptor) ->
                element(
                    elementName = field.name,
                    descriptor = descriptor,
                    annotations = listOf(ProtoNumber(field.tag))
                )
            }
        }

        return proxyDescriptor
    }
}