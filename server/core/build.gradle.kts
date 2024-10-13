plugins {
    id(libs.plugins.conventions.multiplatform.library.get().pluginId)
    alias(libs.plugins.kotlinx.serialization)
}

group = "org.timemates.rrpc"
version = System.getenv("LIB_VERSION") ?: "SNAPSHOT"

dependencies {
    commonMainImplementation(libs.rsocket.server)
    commonMainImplementation(libs.kotlinx.serialization.proto) 
    commonMainApi(projects.common.core)

    commonMainImplementation(libs.ktor.server.websockets) 

    commonMainImplementation(libs.ktor.server.core) 
}

mavenPublishing {
    coordinates(
        groupId = "org.timemates.rrpc",
        artifactId = "server-core",
        version = System.getenv("LIB_VERSION") ?: return@mavenPublishing,
    )

    pom {
        name.set("RRpcroto Server Core")
        description.set("Multiplatform Kotlin core library for RRpcroto servers.")
    }
}
