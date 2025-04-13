@file:OptIn(ExperimentalInterceptorsApi::class, InternalRRpcAPI::class)

package app.timemate.rsp.common.test

import com.google.protobuf.ProtoEmpty
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import app.timemate.rrpc.Single
import app.timemate.rrpc.annotations.ExperimentalInterceptorsApi
import app.timemate.rrpc.annotations.InternalRRpcAPI
import app.timemate.rrpc.instances.InstanceContainer
import app.timemate.rrpc.interceptors.InterceptorContext
import app.timemate.rrpc.interceptors.Interceptors
import app.timemate.rrpc.interceptors.RRpcInterceptor
import app.timemate.rrpc.metadata.ClientMetadata
import app.timemate.rrpc.metadata.ServerMetadata
import app.timemate.rrpc.options.OptionsWithValue
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