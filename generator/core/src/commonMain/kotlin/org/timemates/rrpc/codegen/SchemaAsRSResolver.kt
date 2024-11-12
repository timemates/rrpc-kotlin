package org.timemates.rrpc.codegen

import com.squareup.wire.schema.ProtoMember
import com.squareup.wire.schema.ProtoType
import com.squareup.wire.schema.Schema
import org.timemates.rrpc.common.schema.*
import org.timemates.rrpc.common.schema.value.RMDeclarationUrl

public fun Schema.asRSResolver(): RSResolver {
    return SchemaAsRSResolver(this)
}

private class SchemaAsRSResolver(
    private val schema: Schema,
) : RSResolver {
    override fun resolveField(typeMemberUrl: RSTypeMemberUrl): RSField? {
        return schema.getField(
            ProtoMember.get(
                ProtoType.get(typeMemberUrl.typeUrl.value),
                typeMemberUrl.memberName
            )
        )?.asRMField()
    }

    override fun resolveType(url: RMDeclarationUrl): RSType? {
        return schema.getType(url.value)?.asRMType()
    }

    override fun resolveFileOf(url: RMDeclarationUrl): RSFile? {
        return schema.protoFile(ProtoType.get(url.value))?.asRMFile()
    }

    override fun resolveService(url: RMDeclarationUrl): RSService? {
        return schema.getService(url.value)?.asRMService()
    }

    override fun resolveAvailableFiles(): Sequence<RSFile> {
        return schema.protoFiles.asSequence().map { it.asRMFile() }
    }

    override fun resolveAllServices(): Sequence<RSService> {
        return schema.protoFiles.asSequence().flatMap { it.services }.map { it.asRMService() }
    }

    override fun resolveAllTypes(): Sequence<RSType> {
        return schema.protoFiles.asSequence().flatMap { it.types }.map { it.asRMType() }
    }
}