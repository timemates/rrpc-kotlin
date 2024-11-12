package org.timemates.rrpc.generator.kotlin.dyser

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlin.properties.Delegates

internal class SerialDescriptorProxy : SerialDescriptor {
    var descriptor: SerialDescriptor by Delegates.notNull()

    @ExperimentalSerializationApi
    override val serialName: String
        get() = descriptor.serialName

    @ExperimentalSerializationApi
    override val kind: SerialKind
        get() = descriptor.kind

    @ExperimentalSerializationApi
    override val elementsCount: Int
        get() = descriptor.elementsCount

    @ExperimentalSerializationApi
    override fun getElementName(index: Int): String {
        return descriptor.getElementName(index)
    }

    @ExperimentalSerializationApi
    override fun getElementIndex(name: String): Int {
        return descriptor.getElementIndex(name)
    }

    @ExperimentalSerializationApi
    override fun getElementAnnotations(index: Int): List<Annotation> {
        return descriptor.getElementAnnotations(index)
    }

    @ExperimentalSerializationApi
    override fun getElementDescriptor(index: Int): SerialDescriptor {
        return descriptor.getElementDescriptor(index)
    }

    @ExperimentalSerializationApi
    override fun isElementOptional(index: Int): Boolean {
        return descriptor.isElementOptional(index)
    }
}