package org.timemates.rrpc.generator.kotlin.dyser

import com.google.protobuf.*
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.protobuf.ProtoNumber
import org.timemates.rrpc.common.schema.RMResolver
import org.timemates.rrpc.common.schema.RMType
import org.timemates.rrpc.common.schema.value.RMDeclarationUrl

public class MessageSerializerFactory(
    private val resolver: RMResolver,
) {
    private val descriptorsCache: MutableMap<RMDeclarationUrl, SerialDescriptor> = mutableMapOf()
    private val serializersCache: MutableMap<RMDeclarationUrl, KSerializer<*>> = mutableMapOf()

    public fun getMessageSerializer(message: RMType.Message): KSerializer<MessageValues> {
        return serializersCache.getOrPut(message.typeUrl) {
            MessageValuesSerializer(message, this, resolver, getOrCreateMessageDescriptor(message))
        } as MessageValuesSerializer
    }

    public fun getDescriptor(type: RMType): SerialDescriptor {
        return when (type) {
            is RMType.Enclosing -> ProtoEmpty.serializer().descriptor
            is RMType.Enum -> getOrCreateEnumDescriptor(type)
            is RMType.Message -> getOrCreateMessageDescriptor(type)
        }
    }

    @OptIn(InternalSerializationApi::class)
    private fun getOrCreateEnumDescriptor(enum: RMType.Enum): SerialDescriptor {
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
    private fun getOrCreateMessageDescriptor(message: RMType.Message): SerialDescriptor {
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
                    RMDeclarationUrl.ACK, RMDeclarationUrl.EMPTY -> ProtoAny.serializer().descriptor
                    RMDeclarationUrl.INT32 -> Int.serializer().descriptor
                    RMDeclarationUrl.INT64 -> Long.serializer().descriptor
                    RMDeclarationUrl.BOOL -> Boolean.serializer().descriptor
                    RMDeclarationUrl.DOUBLE -> Double.serializer().descriptor
                    RMDeclarationUrl.FLOAT -> Float.serializer().descriptor
                    RMDeclarationUrl.STRUCT -> ProtoStruct.serializer().descriptor
                    RMDeclarationUrl.STRUCT_VALUE -> ProtoStructValue.serializer().descriptor
                    RMDeclarationUrl.STRUCT_MAP -> TODO()
                    RMDeclarationUrl.DURATION -> ProtoDuration.serializer().descriptor
                    RMDeclarationUrl.TIMESTAMP -> ProtoTimestamp.serializer().descriptor
                    RMDeclarationUrl.UINT32 -> UInt.serializer().descriptor
                    RMDeclarationUrl.UINT64 -> ULong.serializer().descriptor
                    else -> {
                        when (val type = resolver.resolveType(field.typeUrl)!!) {
                            is RMType.Enclosing -> ProtoEmpty.serializer().descriptor
                            is RMType.Message -> getOrCreateMessageDescriptor(type)
                            is RMType.Enum -> getOrCreateEnumDescriptor(type)
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