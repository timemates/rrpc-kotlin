package org.timemates.rrpc.common.schema

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import org.timemates.rrpc.common.schema.value.RMDeclarationUrl

@Serializable
public sealed interface RSType : RSNode, Documentable {
    @ProtoNumber(1)
    public val name: String
    @ProtoNumber(2)
    public val typeUrl: RMDeclarationUrl
    @ProtoNumber(3)
    public val nestedTypes: List<RSType>
    @ProtoNumber(4)
    public val nestedExtends: List<RSExtend>
    @ProtoNumber(5)
    public override val documentation: String?
    @ProtoNumber(6)
    public val options: RSOptions

    @SerialName("ENUM")
    public class Enum(
        @ProtoNumber(1)
        override val name: String,
        @ProtoNumber(7)
        public val constants: List<RSEnumConstant>,
        @ProtoNumber(5)
        override val documentation: String?,
        @ProtoNumber(6)
        public override val options: RSOptions,
        @ProtoNumber(3)
        override val nestedTypes: List<RSType>,
        @ProtoNumber(2)
        override val typeUrl: RMDeclarationUrl,
        @ProtoNumber(4)
        override val nestedExtends: List<RSExtend>,
    ) : RSType

    @SerialName("MESSAGE")
    public class Message(
        @ProtoNumber(1)
        override val name: String,
        @ProtoNumber(5)
        override val documentation: String?,
        @ProtoNumber(8)
        public val fields: List<RSField>,
        @ProtoNumber(9)
        public val oneOfs: List<RSOneOf>,
        @ProtoNumber(6)
        public override val options: RSOptions,
        @ProtoNumber(2)
        override val typeUrl: RMDeclarationUrl,
        @ProtoNumber(3)
        override val nestedTypes: List<RSType>,
        @ProtoNumber(4)
        override val nestedExtends: List<RSExtend>,
    ) : RSType {
        public val allFields: List<RSField> get() = fields + oneOfs.flatMap { it.fields }

        /**
         * Gets [RSField] in current [RSType.Message] by given [tag].
         * If a field with a tag persists in the oneof field – oneof field is returned,
         *  where the field occurs.
         */
        public fun field(tag: Int): RSField? {
            return fields.firstOrNull { field ->
                field.tag == tag
            }
        }

        /**
         * Gets [RSField] in current [RSType.Message] by given [name].
         * If a field with a tag persists in the oneof field – oneof field is returned,
         *  where the field occurs.
         */
        public fun field(name: String): RSField? {
            return fields.firstOrNull { field ->
                field.name == name
            }
        }

        public override fun equals(other: Any?): Boolean {
            return other is Message && other.typeUrl == typeUrl
        }

        override fun hashCode(): Int {
            return typeUrl.hashCode()
        }
    }

    @SerialName("ENCLOSING_TYPE")
    public class Enclosing(
        @ProtoNumber(1)
        override val name: String,
        @ProtoNumber(5)
        override val documentation: String?,
        @ProtoNumber(2)
        override val typeUrl: RMDeclarationUrl,
        @ProtoNumber(3)
        override val nestedTypes: List<RSType>,
        @ProtoNumber(4)
        override val nestedExtends: List<RSExtend>,
        @ProtoNumber(6)
        override val options: RSOptions = RSOptions.EMPTY
    ) : RSType
}