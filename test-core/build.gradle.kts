plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.hilt)

    kotlin("kapt")
//    alias(libs.plugins.kotlinAndroid)
}

apply(from = "$rootDir/compose-module.gradle")

android {
    namespace = "com.esteban.ruano.navigation"
}

dependencies {
    implementation(project(":core"))


//    api(platform(libs.compose.bom))



    api(libs.navigation.testing)
    api(libs.junit)
    api(libs.compose.ui.test)
    api(libs.ui.test.manifest)
    api(libs.junit.android.ext)
    api(libs.test.runner)
//    api(libs.hilt.testing)
//    api(libs.espresso)
    api(libs.mockwebserver)

    // Hilt
    api(libs.hilt.android)
    "kapt"(libs.hilt.android.compiler)
}
