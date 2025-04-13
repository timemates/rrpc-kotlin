package com.google.protobuf

import kotlinx.serialization.Serializable
import app.timemate.rrpc.ProtoType

@Serializable
public class ProtoEmpty private constructor(): ProtoType {
    public companion object : ProtoType.Definition<ProtoEmpty> {
        override val url: String
            get() = "type.googleapis.com/google.protobuf.Empty"
        override val Default: ProtoEmpty = ProtoEmpty()
    }

    override val definition: ProtoType.Definition<*>
        get() = Companion

    override fun toString(): String {
        return "ProtoEmpty()"
    }
}