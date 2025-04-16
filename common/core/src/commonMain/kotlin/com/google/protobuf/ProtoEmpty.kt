package com.google.protobuf

import kotlinx.serialization.Serializable
import app.timemate.rrpc.RSProtoType

@Serializable
public class ProtoEmpty private constructor(): RSProtoType {
    public companion object : RSProtoType.Definition<ProtoEmpty> {
        override val url: String
            get() = "type.googleapis.com/google.protobuf.Empty"
        override val Default: ProtoEmpty = ProtoEmpty()
    }

    override val definition: RSProtoType.Definition<*>
        get() = Companion

    override fun toString(): String {
        return "ProtoEmpty()"
    }
}