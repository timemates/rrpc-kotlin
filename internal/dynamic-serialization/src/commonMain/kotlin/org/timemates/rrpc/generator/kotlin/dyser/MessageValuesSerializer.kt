package org.timemates.rrpc.generator.kotlin.dyser

import com.google.protobuf.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.timemates.rrpc.common.schema.RSResolver
import org.timemates.rrpc.common.schema.RSType
import org.timemates.rrpc.common.schema.value.RMDeclarationUrl

internal class MessageValuesSerializer(
    private val type: RSType.Message,
    private val factory: MessageSerializerFactory,
    private val resolver: RSResolver,
    override val descriptor: SerialDescriptor,
) : KSerializer<MessageValues> {

    override fun serialize(encoder: Encoder, value: MessageValues) {
        val compositeEncoder = encoder.beginStructure(descriptor)

        type.allFields.asSequence().sortedBy { it.tag }.forEach { field ->
            if (value.isNull(field.tag)) {
                val serializer = getSerializer(field.typeUrl, factory, resolver)
                return@forEach compositeEncoder.encodeNullableSerializableElement(
                    descriptor, field.tag, serializer, null,
                )
            }

            if (field.isRepeated) {
                val typeSerializer = getSerializer(field.typeUrl, factory, resolver)
                val serializer = ListSerializer(typeSerializer)

                compositeEncoder.encodeSerializableElement(
                    this.descriptor,
                    field.tag,
                    serializer,
                    value.getList(field.tag)
                )
            } else if (field.typeUrl.isMap) {
                val firstTypeSerializer = getSerializer(field.typeUrl.firstTypeArgument!!, factory, resolver)
                val secondTypeSerializer = getSerializer(field.typeUrl.secondTypeArgument!!, factory, resolver)

                val serializer = MapSerializer(firstTypeSerializer, secondTypeSerializer)

                compositeEncoder.encodeSerializableElement(
                    this.descriptor,
                    field.tag,
                    serializer,
                    value.getMap(field.tag),
                )
            } else {
                val serializer = getSerializer(field.typeUrl, factory, resolver)
                compositeEncoder.encodeSerializableElement(
                    this.descriptor,
                    field.tag,
                    serializer,
                    when (field.typeUrl) {
                        RMDeclarationUrl.STRING -> value.getString(field.tag)
                        RMDeclarationUrl.BYTES -> value.getBytes(field.tag)
                        RMDeclarationUrl.INT32 -> value.getInt(field.tag)
                        RMDeclarationUrl.INT64 -> value.getLong(field.tag)
                        RMDeclarationUrl.UINT32 -> value.getUInt(field.tag)
                        RMDeclarationUrl.UINT64 -> value.getULong(field.tag)
                        RMDeclarationUrl.BOOL -> value.getBoolean(field.tag)
                        RMDeclarationUrl.FLOAT -> value.getFloat(field.tag)
                        RMDeclarationUrl.DOUBLE -> value.getDouble(field.tag)
                        RMDeclarationUrl.ACK, RMDeclarationUrl.EMPTY -> ProtoEmpty.serializer()
                        else -> {
                            val type = resolver.resolveType(field.typeUrl)!!
                            return compositeEncoder.encodeNullableSerializableElement(
                                this.descriptor,
                                field.tag,
                                serializer,
                                when (type) {
                                    is RSType.Enclosing -> Unit
                                    is RSType.Enum -> value.getInt(field.tag)
                                    is RSType.Message -> value.getMessage(field.tag)
                                },
                            )
                        }
                    }
                )
            }
        }

        compositeEncoder.endStructure(descriptor)
    }
    
    override fun deserialize(decoder: Decoder): MessageValues {
        val composite = decoder.beginStructure(descriptor)
        return MessageValues.create {
            type.allFields.forEach { field ->
                val value = if (field.isRepeated) {
                    composite.decodeSerializableElement(
                        descriptor = descriptor,
                        index = field.tag,
                        deserializer = ListSerializer(getSerializer(field.typeUrl, factory, resolver)),
                    )
                } else if (field.typeUrl.isMap) {
                    composite.decodeSerializableElement(
                        descriptor = descriptor,
                        index = field.tag,
                        deserializer = MapSerializer(
                            getSerializer(field.typeUrl.firstTypeArgument!!, factory, resolver),
                            getSerializer(field.typeUrl.secondTypeArgument!!, factory, resolver),
                        ),
                    )
                } else if (field.typeUrl.isScalar) {
                    composite.decodeSerializableElement(
                        descriptor = descriptor,
                        index = field.tag,
                        deserializer = getSerializer(field.typeUrl, factory, resolver),
                    )
                } else {
                    composite.decodeNullableSerializableElement(
                        descriptor = descriptor,
                        index = field.tag,
                        deserializer = getSerializer(field.typeUrl, factory, resolver),
                    )
                }

                setRaw(field.tag, value)
            }
        }
    }
}


// TODO better handling of primitives to avoid overhead while using encodeNullableSerializableElement for now we keep it for simplicity.
private fun getSerializer(
    url: RMDeclarationUrl,
    factory: MessageSerializerFactory,
    resolver: RSResolver,
): KSerializer<Any> {
    return when (url) {
        RMDeclarationUrl.STRING -> String.serializer()
        RMDeclarationUrl.BYTES -> ByteArraySerializer()
        RMDeclarationUrl.INT32 -> Int.serializer()
        RMDeclarationUrl.INT64 -> Long.serializer()
        RMDeclarationUrl.UINT32 -> UInt.serializer()
        RMDeclarationUrl.UINT64 -> ULong.serializer()
        RMDeclarationUrl.BOOL -> Boolean.serializer()
        RMDeclarationUrl.FLOAT -> Float.serializer()
        RMDeclarationUrl.DOUBLE -> Double.serializer()
        RMDeclarationUrl.ACK, RMDeclarationUrl.EMPTY -> ProtoEmpty.serializer()
        else -> {
            when (val type = resolver.resolveType(url)!!) {
                is RSType.Enum -> Int.serializer()
                is RSType.Enclosing -> ProtoEmpty.serializer()
                is RSType.Message -> factory.getMessageSerializer(type)
            }
        }
    } as KSerializer<Any>
}