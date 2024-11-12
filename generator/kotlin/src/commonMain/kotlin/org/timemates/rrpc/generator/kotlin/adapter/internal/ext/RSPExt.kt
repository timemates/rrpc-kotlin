package org.timemates.rrpc.generator.kotlin.adapter.internal.ext

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import org.timemates.rrpc.codegen.typemodel.LibClassNames
import org.timemates.rrpc.common.schema.Language
import org.timemates.rrpc.common.schema.RSResolver
import org.timemates.rrpc.common.schema.StreamableRMTypeUrl
import org.timemates.rrpc.common.schema.annotations.NonPlatformSpecificAccess
import org.timemates.rrpc.common.schema.value.RMDeclarationUrl

@OptIn(NonPlatformSpecificAccess::class)
internal fun RMDeclarationUrl.asClassName(resolver: RSResolver): ClassName {
    return when (this) {
        RMDeclarationUrl.ANY -> ClassName("com.google.protobuf", "ProtoAny")
        RMDeclarationUrl.TIMESTAMP -> ClassName("com.google.protobuf", "ProtoTimestamp")
        RMDeclarationUrl.DURATION -> ClassName("com.google.protobuf", "ProtoDuration")
        RMDeclarationUrl.STRUCT_MAP -> ClassName("com.google.protobuf", "ProtoStruct")
        RMDeclarationUrl.EMPTY -> ClassName("com.google.protobuf", "ProtoEmpty")
        RMDeclarationUrl.STRING_VALUE -> LibClassNames.Wrappers.STRING_VALUE
        RMDeclarationUrl.INT32_VALUE -> LibClassNames.Wrappers.INT32_VALUE
        RMDeclarationUrl.INT64_VALUE -> LibClassNames.Wrappers.INT64_VALUE
        RMDeclarationUrl.FLOAT_VALUE -> LibClassNames.Wrappers.FLOAT_VALUE
        RMDeclarationUrl.DOUBLE_VALUE -> LibClassNames.Wrappers.DOUBLE_VALUE
        RMDeclarationUrl.UINT32_VALUE -> LibClassNames.Wrappers.UINT32_VALUE
        RMDeclarationUrl.UINT64_VALUE -> LibClassNames.Wrappers.UINT64_VALUE
        RMDeclarationUrl.BOOL_VALUE -> LibClassNames.Wrappers.BOOL_VALUE
        RMDeclarationUrl.BYTES_VALUE -> LibClassNames.Wrappers.BYTES_VALUE
        else -> {
            val file = resolver.resolveFileOf(this) ?: return ClassName(enclosingTypeOrPackage ?: "", simpleName)

            val packageName: String = file.platformPackageName(Language.KOTLIN).value
            val enclosingName: String = (enclosingTypeOrPackage?.replace(file.packageName.value, "") ?: "")
                .replace("..", ".")

            ClassName(packageName + enclosingName, simpleName)
        }
    }
}

internal fun StreamableRMTypeUrl.asClassName(resolver: RSResolver): TypeName {
    return when (isStreaming) {
        true -> LibClassNames.Flow(type.asClassName(resolver))
        false -> type.asClassName(resolver)
    }
}

@OptIn(NonPlatformSpecificAccess::class)
internal fun RMDeclarationUrl.qualifiedName(resolver: RSResolver): String {
    val packageName = resolver.resolveFileOf(this)?.packageName?.value?.plus(".") ?: ""

    return packageName + simpleName
}