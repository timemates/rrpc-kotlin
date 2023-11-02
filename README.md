# RSocket Proto Generator
Code-generation gradle plugin that generates gRPC-like services from `.proto` files.

> **Warning** <br>
> This project is still under development and it's not production-ready.

## Generation example
### Messages
Gradle plugins generates immutable data class with builder (Serialization is provided using `kotlinx.serialization`):
```kotlin
@Serializable
public class User(
  @ProtoNumber(1)
  public val id: Int = 0,
  @ProtoNumber(2)
  public val name: String = "",
  @ProtoNumber(3)
  public val bio: String = "",
) {
  public companion object {
    public fun create(builder: Builder.() -> User): Unit = Builder().apply(builder).build()
  }

  public class Builder {
    public var id: Int = 0
    public var name: String = ""
    public var bio: String = ""

    public fun build(): User = User(id, name, bio)
  }
}
```

### Services
Everything is the same to the gRPC, but streaming has RSocket-like structure:
```kotlin
public abstract class TestService {
  public abstract fun getTest(request: Test): Test
}
```

## Feedback

For bugs, questions and discussions please use the [GitHub Issues](https://github.com/y9vad9/rsocket-kotlin-router/issues).

## License

This library is licensed under [MIT License](LICENSE). Feel free to use, modify, and distribute it for any purpose.
