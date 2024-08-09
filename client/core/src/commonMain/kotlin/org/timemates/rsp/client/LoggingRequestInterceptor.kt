package org.timemates.rsp.client

import kotlinx.coroutines.flow.onEach
import org.timemates.rsp.*
import org.timemates.rsp.annotations.ExperimentalInterceptorsApi
import org.timemates.rsp.interceptors.InterceptorContext
import org.timemates.rsp.interceptors.RequestInterceptor
import org.timemates.rsp.metadata.ClientMetadata
import java.util.UUID

@OptIn(ExperimentalInterceptorsApi::class)
public class LoggingRequestInterceptor : RequestInterceptor {
    override fun intercept(
        context: InterceptorContext<ClientMetadata>
    ): InterceptorContext<ClientMetadata> {
        val reference = "${context.metadata.serviceName}#${context.metadata.procedureName}"

        when (context.data) {
            // means that request data is not of a streaming type
            is DataVariant.Single -> {
                println("$reference(${context.data.requireSingle()})")
            }

            // means that request data is of a streaming type
            is DataVariant.Streaming -> {
                val id = UUID.randomUUID()
                return context.copy(
                    data = (context.data as DataVariant.Streaming).onEach {
                        println("streaming $id: $reference($it)")
                    }
                )
            }

            // the request was accepted, but something has happened (usually, it's caused
            // by previous interceptor)
            is DataVariant.Failure -> {
                println("Failed to call $reference:")
                (context.data as DataVariant.Failure).exception.printStackTrace()
            }
        }

        return context
    }
}