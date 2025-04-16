package org.timemates.rrpc.test

import TestServiceClient
import app.timemate.rrpc.annotations.ExperimentalInterceptorsApi
import app.timemate.rrpc.client.config.RRpcClientConfig
import app.timemate.rrpc.metadata.generated.timemate.rrpc.AckFileMetadata
import app.timemate.rrpc.server.module.RRpcModule
import app.timemate.rrpc.server.module.rrpcEndpoint
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.rsocket.kotlin.ktor.client.rSocket
import io.rsocket.kotlin.ktor.server.RSocketSupport
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@Suppress("UNCHECKED_CAST")
class IntegrationTest {
    @Suppress("UNCHECKED_CAST")
    @OptIn(ExperimentalInterceptorsApi::class)
    private val serverModule = RRpcModule {
        services {
            register(ServerTestService())
        }

        instances {
            register(SomeValue(0))
        }

        interceptors {
            request(RequestTestInterceptor())
            response(ResponseTestInterceptor())
        }
    }

    private val httpClient = HttpClient(CIO) {
        install(io.ktor.client.plugins.websocket.WebSockets)
        install(io.rsocket.kotlin.ktor.client.RSocketSupport)
    }

    private val serverInstance = embeddedServer(Netty, port = 9393) {
        install(WebSockets)
        install(RSocketSupport)

        routing {
            rrpcEndpoint("rrpc", serverModule)
        }
    }.start(false)

    @OptIn(DelicateCoroutinesApi::class, ExperimentalInterceptorsApi::class)
    private val client = GlobalScope.async {
        val rSocket = httpClient.rSocket("ws://localhost:9393/rrpc")
        TestServiceClient(
            config = RRpcClientConfig.create {
                rsocket(rSocket)
                interceptors {
                    request(RequestTestInterceptor())
                    response(ResponseTestInterceptor())
                }
                instances {
                    register(SomeValue(0))
                }
            }
        )
    }

    @Test
    fun `test run and interceptors`(): Unit = runTest {
        val result = client.await().testMethod(TestMessage.Default)
        assertEquals("test", result.stringField)
        assertEquals(listOf(0.1, 0.2, 3.0), result.repeatedField)
    }
}