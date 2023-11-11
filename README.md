# RSocket Proto Generator

Code-generation gradle plugin that generates gRPC-like services from `.proto` files.

> **Warning** <br>
> This project is still under development and it's not production-ready.

## TODO
- [x] Core
- [x] Code Generation
- [ ] Gradle Plugin
- [ ] Publication
- [ ] Tests

## Example

### Server Builder
There's how you can set up your server with Ktor:
```kotlin
 fun Application.configureServer() {
    routing {
        rSocketServer("/rsocket") {
            interceptor(MyInterceptor())
            service(MyService())

            instances {
                protobuf(
                    ProtoBuf {
                        encodeDefaults = true
                    }
                )
            }
        }
    }
}
```

### Generation example
#### Messages

Gradle plugins generates immutable data class with builder (Serialization is provided using `kotlinx.serialization`):

```kotlin
@Serializable
class User(
    @ProtoNumber(1) val id: Int = 0,
    @ProtoNumber(2) val name: String = "",
    @ProtoNumber(3) val bio: String = "",
) {
    companion object {
        fun create(builder: Builder.() -> User): Unit = Builder().apply(builder).build()
    }

    class Builder {
        var id: Int = 0
        var name: String = ""
        var bio: String = ""

        fun build(): User = User(id, name, bio)
    }
}
```

#### Services

Everything is the same to the gRPC, but streaming has RSocket-like structure:

```kotlin
 abstract class TestService {
    final override val descriptor: ServiceDescriptor = ServiceDescriptor(
        name = "TestService",
        procedures = listOf(
            ProcedureDescriptor.RequestResponse(
                name = "getTest",
                inputSerializer = Test.serializer(),
                outputSerializer = Test.serializer(),
                procedure = { getTest(it as Test) as Test }
            )
        )
    )

    abstract fun getTest(request: Test): Test
}
```

## Feedback

For bugs, questions and discussions please use
the [GitHub Issues](https://github.com/y9vad9/rsocket-kotlin-router/issues).

## License

This library is licensed under [MIT License](LICENSE). Feel free to use, modify, and distribute it for any purpose.
