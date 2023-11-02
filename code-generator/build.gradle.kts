plugins {
    id(libs.plugins.conventions.multiplatform.library.get().pluginId)
}

dependencies {
    commonMainImplementation(libs.squareup.wire.schema)
    commonMainImplementation(libs.squareup.kotlinpoet)
    commonMainImplementation(libs.squareup.okio)

    jvmTestImplementation(libs.kotlin.test)
    jvmTestImplementation(libs.squareup.okio.fakeFs)
}