package org.timemates.rsp.codegen.ext

import com.squareup.kotlinpoet.ClassName
import com.squareup.wire.schema.*

internal fun ProtoType.asClassName(schema: Schema): ClassName {
    val file = schema.protoFile(this) ?: return ClassName(enclosingTypeOrPackage ?: "", simpleName)

    val packageName: String =  file.smartPackage() ?: ""
    val enclosingName: String = (enclosingTypeOrPackage?.replace(file.packageName ?: "", "") ?: "")
        .replace("..", ".")

    return ClassName(packageName + enclosingName, simpleName)
}

internal fun ProtoFile.smartPackage(): String? {
    return wirePackage() ?: javaPackage()?.plus(".")
}

internal fun ProtoType.qualifiedName(schema: Schema): String {
    val packageName = schema.protoFile(this)?.packageName?.plus(".") ?: ""

    return packageName + simpleName
}

/**
 * Determines if the RPC is a request-response type.
 *
 * @return `true` if the RPC is a request-response type, `false` otherwise.
 */
internal val Rpc.isRequestResponse get() = !requestStreaming && !responseStreaming

/**
 * Determines if the Rpc is a request stream.
 *
 * @return true if the Rpc is a request stream, false otherwise.
 */
internal val Rpc.isRequestStream get() = !requestStreaming && responseStreaming

/**
 * Determines if the RPC is a request channel.
 *
 * @return `true` if the RPC is a request channel, `false` otherwise.
 */
internal val Rpc.isRequestChannel get() = requestStreaming && responseStreaming

internal val Options.Companion.deprecated get() = ProtoMember.get(METHOD_OPTIONS, "deprecated")
internal val Options.Companion.retention get() = ProtoMember.get(FIELD_OPTIONS, "retention")

internal val Options.isDeprecated get() = get(Options.deprecated)?.equals("true") == true
internal val Options.isWithSourceRetention get() = get(Options.retention)?.equals("true") == true