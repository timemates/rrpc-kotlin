plugins {
    id(libs.plugins.conventions.multiplatform.library.get().pluginId)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    explicitApi()
}

group = "org.timemates.rrpc"
version = System.getenv("LIB_VERSION") ?: "SNAPSHOT"

dependencies {
    // -- Project --
    commonMainApi(projects.common.schema)

    // -- Serialization --
    commonMainImplementation(libs.kotlinx.serialization.proto)

    // -- Coroutines --
    commonMainImplementation(libs.kotlinx.coroutines)

    // -- SquareUp --
    commonMainImplementation(libs.squareup.wire.schema)
    commonMainImplementation(libs.squareup.kotlinpoet)
    commonMainImplementation(libs.squareup.okio)

    // -- Test --
    commonTestImplementation(libs.kotlin.test)
    commonTestImplementation(libs.squareup.okio.fakeFs)
}


mavenPublishing {
    coordinates(
        groupId = "org.timemates.rrpc",
        artifactId = "generator",
        version = System.getenv("LIB_VERSION") ?: return@mavenPublishing,
    )

    pom {
        name.set("RRpc Code Generator Core")
        description.set("Code-generation library for RRpc servers and clients.")
    }
}