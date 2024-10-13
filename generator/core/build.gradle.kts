plugins {
    id(libs.plugins.conventions.multiplatform.library.get().pluginId)
}

kotlin {
    explicitApi()
}

group = "org.timemates.rrpc"
version = System.getenv("LIB_VERSION") ?: "SNAPSHOT"

dependencies {
    commonMainImplementation(libs.squareup.wire.schema)
    commonMainImplementation(libs.squareup.kotlinpoet)
    commonMainImplementation(libs.squareup.okio)

    commonTestImplementation(libs.kotlin.test)
    commonTestImplementation(libs.squareup.okio.fakeFs)
    commonMainImplementation(projects.common.core)
    commonMainApi(projects.common.metadata)
}

mavenPublishing {
    coordinates(
        groupId = "org.timemates.rrpc",
        artifactId = "generator",
        version = System.getenv("LIB_VERSION") ?: return@mavenPublishing,
    )

    pom {
        name.set("RRpcroto Code Generator Core")
        description.set("Code-generation library for RRpcroto servers and clients.")
    }
}