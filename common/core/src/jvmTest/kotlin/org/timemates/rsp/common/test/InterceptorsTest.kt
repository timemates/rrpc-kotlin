@file:OptIn(ExperimentalInterceptorsApi::class, InternalRRpcAPI::class)

package org.timemates.rsp.common.test

import com.google.protobuf.ProtoEmpty
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.timemates.rrpc.Single
import org.timemates.rrpc.annotations.ExperimentalInterceptorsApi
import org.timemates.rrpc.annotations.InternalRRpcAPI
import org.timemates.rrpc.instances.InstanceContainer
import org.timemates.rrpc.interceptors.InterceptorContext
import org.timemates.rrpc.interceptors.Interceptors
import org.timemates.rrpc.interceptors.RRpcInterceptor
import org.timemates.rrpc.metadata.ClientMetadata
import org.timemates.rrpc.metadata.ServerMetadata
import org.timemates.rrpc.options.OptionsWithValue
import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.test.assertSame

class InterceptorsTest {
    @Test
    fun `runInputInterceptors with no interceptors should return null`(): Unit = runBlocking {
        val interceptors = Interceptors(emptyList(), emptyList())

        assertNull(
            actual = interceptors.runInputInterceptors(
                Single(ProtoEmpty.Default),
                ClientMetadata(),
                OptionsWithValue.EMPTY,
                InstanceContainer(emptyMap()),
            )
        )
    }

    @Test
    fun `runInputInterceptors with interceptors that change context should return actual context`(): Unit = runBlocking {
        val testInterceptor = mockk<RRpcInterceptor<ClientMetadata>>()
        val expectedContext = mockk<InterceptorContext<ClientMetadata>>()
        coEvery { testInterceptor.intercept(any()) } returns expectedContext

        val interceptors = Interceptors(listOf(testInterceptor), emptyList())

        assertSame(
            actual = interceptors.runInputInterceptors(
                Single(ProtoEmpty.Default),
                ClientMetadata(),
                OptionsWithValue.EMPTY,
                InstanceContainer(emptyMap()),
            ),
            expected = expectedContext,
        )
    }

    @Test
    fun `runOutputInterceptors with no interceptors should return the same context early`(): Unit = runBlocking {
        val interceptors = Interceptors(emptyList(), emptyList())

        assertNull(
            actual = interceptors.runOutputInterceptors(
                Single(ProtoEmpty.Default),
                ServerMetadata(),
                OptionsWithValue.EMPTY,
                InstanceContainer(emptyMap()),
            )
        )
    }

    @Test
    fun `runOutputInterceptors with interceptors that change context should return actual context`(): Unit = runBlocking {
        val testInterceptor = mockk<RRpcInterceptor<ServerMetadata>>()
        val expectedContext = mockk<InterceptorContext<ServerMetadata>>()
        coEvery { testInterceptor.intercept(any()) } returns expectedContext

        val interceptors = Interceptors(emptyList(), listOf(testInterceptor))

        assertSame(
            actual = interceptors.runOutputInterceptors(
                Single(ProtoEmpty.Default),
                ServerMetadata(),
                OptionsWithValue.EMPTY,
                InstanceContainer(emptyMap()),
            ),
            expected = expectedContext,
        )
    }
}