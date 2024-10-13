package org.timemates.rrpc.generator.kotlin.ext

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import org.timemates.rrpc.codegen.typemodel.Types
import org.timemates.rrpc.common.metadata.Language
import org.timemates.rrpc.common.metadata.RMResolver
import org.timemates.rrpc.common.metadata.StreamableRMTypeUrl
import org.timemates.rrpc.common.metadata.annotations.NonPlatformSpecificAccess
import org.timemates.rrpc.common.metadata.value.RMTypeUrl

@OptIn(NonPlatformSpecificAccess::class)
internal fun RMTypeUrl.asClassName(resolver: RMResolver): ClassName {
    return when (this) {
        RMTypeUrl.ANY -> ClassName("com.google.protobuf", "ProtoAny")
        RMTypeUrl.TIMESTAMP -> ClassName("com.google.protobuf", "ProtoTimestamp")
        RMTypeUrl.DURATION -> ClassName("com.google.protobuf", "ProtoDuration")
        RMTypeUrl.STRUCT_MAP -> ClassName("com.google.protobuf", "ProtoStruct")
        RMTypeUrl.EMPTY -> ClassName("com.google.protobuf", "ProtoEmpty")
        else -> {
            val file = resolver.resolveFileOf(this) ?: return ClassName(enclosingTypeOrPackage ?: "", simpleName)

            val packageName: String = file.platformPackageName(Language.KOTLIN).value
            val enclosingName: String = (enclosingTypeOrPackage?.replace(file.packageName.value, "") ?: "")
                .replace("..", ".")

            ClassName(packageName + enclosingName, simpleName)
        }
    }
}

internal fun StreamableRMTypeUrl.asClassName(resolver: RMResolver): TypeName {
    return when (isStreaming) {
        true -> Types.Flow(type.asClassName(resolver))
        false -> type.asClassName(resolver)
    }
}

@OptIn(NonPlatformSpecificAccess::class)
internal fun RMTypeUrl.qualifiedName(resolver: RMResolver): String {
    val packageName = resolver.resolveFileOf(this)?.packageName?.value?.plus(".") ?: ""

    return packageName + simpleName
}