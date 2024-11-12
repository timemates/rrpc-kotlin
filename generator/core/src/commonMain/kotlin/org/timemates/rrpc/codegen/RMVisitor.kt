package org.timemates.rrpc.codegen

import org.timemates.rrpc.common.schema.*

/**
 * Visitor interface for traversing RRpc schema objects.
 *
 * @param D The type of data passed during the traversal.
 * @param R The type of result returned from the visit methods.
 */
public interface RMVisitor<D, R> {
    public fun visitFile(file: RSFile, data: D): R
    public fun visitService(service: RSService, data: D): R
    public fun visitType(type: RSType, data: D): R
    public fun visitField(field: RSField, data: D): R
    public fun visitConstant(constant: RSEnumConstant, data: D): R
    public fun visitRpc(rpc: RSRpc, data: D): R
}