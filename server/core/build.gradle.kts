plugins {
    id(libs.plugins.conventions.multiplatform.library.get().pluginId)
    alias(libs.plugins.kotlinx.serialization)
}

group = "org.timemates.rsp"
version = System.getenv("LIB_VERSION") ?: "SNAPSHOT"

dependencies {
    commonMainImplementation(libs.rsocket.server)
    commonMainImplementation(libs.kotlinx.serialization.proto) 
    commonMainImplementation(projects.common.core) 

    commonMainImplementation(libs.ktor.server.websockets) 

    commonMainImplementation(libs.ktor.server.core) 
}

mavenPublishing {
    coordinates(
        groupId = "org.timemates.rsp",
        artifactId = "server-core",
        version = System.getenv("LIB_VERSION") ?: return@mavenPublishing,
    )

    pom {
        name.set("RSProto Server Core")
        description.set("Multiplatform Kotlin core library for RSProto servers.")
    }
}
