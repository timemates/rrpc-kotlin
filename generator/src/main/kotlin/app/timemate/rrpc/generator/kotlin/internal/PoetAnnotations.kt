package app.timemate.rrpc.generator.kotlin.internal

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName

@Suppress("FunctionName")
internal object PoetAnnotations {
    fun ProtoNumber(number: Int): AnnotationSpec =
        AnnotationSpec.Companion.builder(
            ClassName("kotlinx.serialization.protobuf", "ProtoNumber")
        ).addMember(number.toString()).build()

    val Serializable = AnnotationSpec.Companion.builder(ClassName("kotlinx.serialization", "Serializable")).build()

    val Deprecated = AnnotationSpec.Companion.builder(ClassName("kotlin", "Deprecated")).addMember(
        "\"Deprecated in .proto definition.\""
    ).build()

    fun OptIn(typeName: TypeName): AnnotationSpec = AnnotationSpec.Companion.builder(
        ClassName("kotlin", "OptIn")
    ).addMember("%T::class", typeName).build()

    fun Suppress(vararg warnings: String): AnnotationSpec = AnnotationSpec.Companion.builder(Suppress::class)
        .apply {
            warnings.forEach { warning ->
                addMember("\"$warning\"")
            }
        }
        .build()

    val ProtoPacked = AnnotationSpec.Companion.builder(ClassName("kotlinx.serialization.protobuf", "ProtoPacked")).build()
    val ProtoOneOf = AnnotationSpec.Companion.builder(ClassName("kotlinx.serialization.protobuf", "ProtoOneOf")).build()

    fun ProtoType(variant: String) = AnnotationSpec.builder(ClassName("kotlinx.serialization.protobuf", "ProtoType"))
        .addMember("%T.$variant", ClassName("kotlinx.serialization.protobuf", "ProtoIntegerType"))
        .build()

    val InternalMetadataApi = AnnotationSpec.builder(ClassName("app.timemate.rrpc.metadata.common.annotation", "InternalMetadataApi"))
        .build()

    val InternalRRpcAPI = ClassName("app.timemate.rrpc.annotations", "InternalRRpcAPI")
}