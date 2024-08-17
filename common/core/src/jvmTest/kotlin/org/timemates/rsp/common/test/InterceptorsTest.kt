@file:OptIn(ExperimentalInterceptorsApi::class, InternalRSProtoAPI::class)

package org.timemates.rsp.common.test

import io.mockk.every
import io.mockk.mockk
import org.timemates.rsp.DataVariant
import org.timemates.rsp.Single
import org.timemates.rsp.annotations.ExperimentalInterceptorsApi
import org.timemates.rsp.annotations.InternalRSProtoAPI
import org.timemates.rsp.instances.InstanceContainer
import org.timemates.rsp.interceptors.Interceptor
import org.timemates.rsp.interceptors.InterceptorContext
import org.timemates.rsp.interceptors.Interceptors
import org.timemates.rsp.metadata.ClientMetadata
import org.timemates.rsp.metadata.ServerMetadata
import org.timemates.rsp.options.Options
import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.test.assertSame

class InterceptorsTest {
    @Test
    fun `runInputInterceptors with no interceptors should return null`() {
        val interceptors = Interceptors(emptyList(), emptyList())

        assertNull(
            actual = interceptors.runInputInterceptors(
                Single(""),
                ClientMetadata(),
                Options.EMPTY,
                InstanceContainer(emptyMap()),
            )
        )
    }

    @Test
    fun `runInputInterceptors with interceptors that change context should return actual context`() {
        val testInterceptor = mockk<Interceptor<ClientMetadata>>()
        val expectedContext = mockk<InterceptorContext<ClientMetadata>>()
        every { testInterceptor.intercept(any()) } returns expectedContext

        val interceptors = Interceptors(listOf(testInterceptor), emptyList())

        assertSame(
            actual = interceptors.runInputInterceptors(
                Single(""),
                ClientMetadata(),
                Options.EMPTY,
                InstanceContainer(emptyMap()),
            ),
            expected = expectedContext,
        )
    }

    @Test
    fun `runOutputInterceptors with no interceptors should return the same context early`() {
        val interceptors = Interceptors(emptyList(), emptyList())

        assertNull(
            actual = interceptors.runOutputInterceptors(
                Single(""),
                ServerMetadata(),
                Options.EMPTY,
                InstanceContainer(emptyMap()),
            )
        )
    }

    @Test
    fun `runOutputInterceptors with interceptors that change context should return actual context`() {
        val testInterceptor = mockk<Interceptor<ServerMetadata>>()
        val expectedContext = mockk<InterceptorContext<ServerMetadata>>()
        every { testInterceptor.intercept(any()) } returns expectedContext

        val interceptors = Interceptors(emptyList(), listOf(testInterceptor))

        assertSame(
            actual = interceptors.runOutputInterceptors(
                Single(""),
                ServerMetadata(),
                Options.EMPTY,
                InstanceContainer(emptyMap()),
            ),
            expected = expectedContext,
        )
    }
}