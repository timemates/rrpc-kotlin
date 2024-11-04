plugins {
    id(libs.plugins.conventions.multiplatform.core.get().pluginId)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.jetbrains.compiler.compose)
}

dependencies {
    commonMainImplementation(libs.jakewharton.mosaic)
    commonMainImplementation(libs.squareup.okio)
}