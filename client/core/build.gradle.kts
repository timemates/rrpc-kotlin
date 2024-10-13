plugins {
    id(libs.plugins.conventions.multiplatform.library.get().pluginId)
    alias(libs.plugins.kotlinx.serialization)
}

group = "org.timemates.rrpc"
version = System.getenv("LIB_VERSION") ?: "SNAPSHOT"

dependencies {
    commonMainImplementation(libs.rsocket.client)
    commonMainApi(projects.common.core)

    jvmTestImplementation(libs.kotlin.test)
    jvmTestImplementation(libs.mockk)
}

kotlin {
//    js(IR) {
//        browser()
//        nodejs()
//    }
//    iosArm64()
//    iosX64()
//    iosSimulatorArm64()
}

mavenPublishing {
    coordinates(
        groupId = "org.timemates.rrpc",
        artifactId = "client-core",
        version = System.getenv("LIB_VERSION") ?: return@mavenPublishing,
    )

    pom {
        name.set("RRpcroto Client Core")
        description.set("Multiplatform Kotlin core library for RRpcroto clients.")
    }
}
