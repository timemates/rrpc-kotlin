package org.timemates.rrpc.test.metadata

import TestServiceClient
import app.timemate.rrpc.annotations.ExperimentalInterceptorsApi
import app.timemate.rrpc.client.config.RRpcClientConfig
import app.timemate.rrpc.metadata.common.communication.GetAllFilesRequest
import app.timemate.rrpc.metadata.generated.Test23MetadataModule
import app.timemate.rrpc.server.module.RRpcModule
import app.timemate.rrpc.server.module.rrpcEndpoint
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.rsocket.kotlin.ktor.client.rSocket
import io.rsocket.kotlin.ktor.server.RSocketSupport
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.timemates.rrpc.client.schema.SchemaMetadataServiceClient
import org.timemates.rrpc.server.schema.DefaultSchemaMetadataService
import org.timemates.rrpc.test.RequestTestInterceptor
import org.timemates.rrpc.test.ResponseTestInterceptor
import org.timemates.rrpc.test.ServerTestService
import org.timemates.rrpc.test.SomeValue
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@Suppress("UNCHECKED_CAST")
class IntegrationTest {
    @Suppress("UNCHECKED_CAST")
    @OptIn(ExperimentalInterceptorsApi::class)
    private val serverModule = RRpcModule {
        services {
            register(DefaultSchemaMetadataService(Test23MetadataModule()))
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
        SchemaMetadataServiceClient(
            config = RRpcClientConfig.create {
                rsocket(rSocket)
            }
        )
    }

    @Test
    fun `test provides all files`(): Unit = runTest {
        val result = client.await().getAllFiles(GetAllFilesRequest.Default)
            .map { it.files }
            .toList()
            .flatten()
        assertEquals(13, result.size)
    }

    @AfterTest
    fun tearDown() {
        serverInstance.stop()
    }
}