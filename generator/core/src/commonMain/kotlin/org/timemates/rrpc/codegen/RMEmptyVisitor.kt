package org.timemates.rrpc.codegen

import org.timemates.rrpc.common.schema.*

public abstract class RMEmptyVisitor<D, R> : RMVisitor<D, R> {
    public abstract fun defaultHandler(node: RSNode, data: D): R

    override fun visitFile(file: RSFile, data: D): R {
        return defaultHandler(file, data)
    }

    override fun visitService(service: RSService, data: D): R {
        return defaultHandler(service, data)
    }

    override fun visitType(type: RSType, data: D): R {
        return defaultHandler(type, data)
    }

    override fun visitRpc(rpc: RSRpc, data: D): R {
        return defaultHandler(rpc, data)
    }

    override fun visitField(field: RSField, data: D): R {
        return defaultHandler(field, data)
    }

    override fun visitConstant(constant: RSEnumConstant, data: D): R {
        return defaultHandler(constant, data)
    }
}