package io.timemates.rsproto.codegen.types

import com.squareup.kotlinpoet.TypeSpec
import com.squareup.wire.schema.*

internal object TypeTransformer {
    fun transform(incoming: Type, schema: Schema): TypeSpec {
        return when (incoming) {
            is MessageType -> MessageTypeTransformer.transform(incoming, schema)
            is EnumType -> EnumTypeTransformer.transform(incoming, schema)
            is EnclosingType -> EnclosingTypeTransformer.transform(incoming, schema)
        }
    }

}