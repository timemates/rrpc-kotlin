plugins {
    id(libs.plugins.conventions.jvm.get().pluginId)
}

kotlin {
    explicitApi()
}

dependencies {
    implementation(libs.squareup.wire.schema)
    implementation(libs.squareup.kotlinpoet)
    implementation(libs.squareup.okio)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.squareup.okio.fakeFs)
}