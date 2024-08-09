package org.timemates.rsp

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.jvm.JvmInline

/**
 * Represents a value that can be in one of three states: a single value, a streaming value, or a failure.
 *
 * @param T The type of the value.
 */
public sealed interface DataVariant<T : Any> {

    /**
     * Checks if the other [DataVariant] is of the same type as this one.
     *
     * @param other The other [DataVariant] to compare with.
     * @return `true` if the other [DataVariant] is of the same type, `false` otherwise.
     */
    public fun isSameType(other: DataVariant<*>): Boolean

    /**
     * Represents a single value.
     *
     * @param T The type of the value.
     * @property value The single value.
     */
    @JvmInline
    public value class Single<T : Any>(public val value: T) : DataVariant<T> {
        override fun isSameType(other: DataVariant<*>): Boolean {
            return other is Single
        }

        override fun toString(): String {
            return "DataVariant.Single(value=$value)"
        }
    }

    /**
     * Represents a streaming value.
     *
     * @param T The type of the values in the stream.
     * @property flow The flow of values.
     */
    @JvmInline
    public value class Streaming<T : Any>(public val flow: Flow<T>) : DataVariant<T> {
        override fun isSameType(other: DataVariant<*>): Boolean {
            return other is Streaming
        }
        override fun toString(): String {
            return "DataVariant.Streaming(flow=$flow)"
        }
    }

    /**
     * Represents a failure state, usually relevant only for clients as the server can return an error without a body.
     * In addition, it's applicable only if original expected type is [Single]. For [Streaming] use flow's
     * operators to handle failures.
     *
     * @param T The type of the value that was expected.
     * @property exception The exception representing the failure.
     */
    @JvmInline
    public value class Failure<T : Any>(public val exception: Exception) : DataVariant<T> {
        override fun isSameType(other: DataVariant<*>): Boolean {
            return other is Failure
        }
    }
}

/**
 * Checks if the [DataVariant] is a single value.
 *
 * @return `true` if the variant is a [DataVariant.Single], `false` otherwise.
 */
@OptIn(ExperimentalContracts::class)
public fun <T : Any> DataVariant<T>.isSingle(): Boolean {
    contract {
        returns(true) implies (this@isSingle is DataVariant.Single<T>)
        returns(false) implies (this@isSingle !is DataVariant.Single<T>)
    }

    return this is DataVariant.Single<T>
}

/**
 * Checks if the [DataVariant] is a streaming value.
 *
 * @return `true` if the variant is a [DataVariant.Streaming], `false` otherwise.
 */
@OptIn(ExperimentalContracts::class)
public fun <T : Any> DataVariant<T>.isStreaming(): Boolean {
    contract {
        returns(true) implies (this@isStreaming is DataVariant.Streaming<T>)
        returns(false) implies (this@isStreaming !is DataVariant.Streaming<T>)
    }

    return this is DataVariant.Streaming<T>
}

/**
 * Checks if the [DataVariant] represents a failure.
 *
 * @return `true` if the variant is a [DataVariant.Failure], `false` otherwise.
 */
@OptIn(ExperimentalContracts::class)
public fun <T : Any> DataVariant<T>.isNotPresent(): Boolean {
    contract {
        returns(true) implies (this@isNotPresent is DataVariant.Failure)
        returns(false) implies (this@isNotPresent !is DataVariant.Failure)
    }

    return this is DataVariant.Failure
}

/**
 * Requires that the [DataVariant] is a single value and returns it.
 *
 * @return The single value.
 * @throws IllegalStateException if the variant is not a [DataVariant.Single].
 */
@OptIn(ExperimentalContracts::class)
public fun <T : Any> DataVariant<T>.requireSingle(): T {
    contract {
        returns() implies (this@requireSingle is DataVariant.Single<T>)
    }
    return if (isSingle()) this.value else error("Expected a single value, but got: $this.")
}

/**
 * Requires that the [DataVariant] is a streaming value and returns it.
 *
 * @return The streaming value.
 * @throws IllegalStateException if the variant is not a [DataVariant.Streaming].
 */
@OptIn(ExperimentalContracts::class)
public fun <T : Any> DataVariant<T>.requireStreaming(): Flow<T> {
    contract {
        returns() implies (this@requireStreaming is DataVariant.Streaming<T>)
    }
    return if (isStreaming()) this.flow else error("Expected a streaming value, but was not.")
}

/**
 * Applies the given [action] to each element of the [DataVariant.Streaming] flow.
 *
 * @param action The action to apply to each element.
 * @return A new [DataVariant.Streaming] with the action applied to each element.
 */
public fun <T : Any> DataVariant.Streaming<T>.onEach(
    action: suspend (T) -> Unit
): DataVariant.Streaming<T> {
    return DataVariant.Streaming(flow.onEach(action))
}