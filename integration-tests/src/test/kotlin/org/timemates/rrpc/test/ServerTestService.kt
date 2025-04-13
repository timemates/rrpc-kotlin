package org.timemates.rrpc.test

import TestMessage
import TestService
import app.timemate.rrpc.server.RequestContext

class ServerTestService : TestService() {
    override suspend fun testMethod(
        context: RequestContext,
        request: TestMessage,
    ): TestMessage {
        return TestMessage {
            testOneof = TestMessage.TestOneofOneOf.Name("Test")
            stringField = "test"
            child = TestMessage.Default
            mapField = mapOf("test" to TestMessage.NestedMessage.Default)
            repeatedField = listOf(.1, .2, 3.0)
        }
    }
}