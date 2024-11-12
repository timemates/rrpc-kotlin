plugins {
    id(libs.plugins.conventions.multiplatform.core.get().pluginId)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.jetbrains.compiler.compose)
}

dependencies {
    // -- Compose --
    commonMainImplementation(compose.ui)
    commonMainImplementation(compose.material3)
    commonMainImplementation(compose.materialIconsExtended)

    // -- Decompose --
    commonMainImplementation(libs.decompose)
    commonMainImplementation(libs.decompose.jetbrains.compose)

    // -- FlowMVI --
    commonMainImplementation(libs.flowmvi.core)
    commonMainImplementation(libs.flowmvi.compose)
    commonMainImplementation(libs.flowmvi.essenty.compose)

    // -- SquareUp --
    commonMainImplementation(libs.squareup.okio)
}