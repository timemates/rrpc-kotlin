package io.timemates.rsproto.codegen.types

import com.squareup.kotlinpoet.TypeSpec
import com.squareup.wire.schema.EnclosingType
import com.squareup.wire.schema.EnumType
import com.squareup.wire.schema.MessageType
import com.squareup.wire.schema.Type

internal object TypeTransformer {
    fun transform(incoming: Type): TypeSpec {
        return when (incoming) {
            is MessageType -> MessageTypeTransformer.transform(incoming)
            is EnumType -> io.timemates.rsproto.codegen.types.EnumTypeTransformer.transform(incoming)
            is EnclosingType -> EnclosingTypeTransformer.transform(incoming)
        }
    }

}