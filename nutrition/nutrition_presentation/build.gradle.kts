plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeCompiler)
//    alias(libs.plugins.kotlinAndroid)
}

apply(from = "$rootDir/compose-module.gradle")

android {
    namespace = "com.esteban.ruano.nutrition_presentation"
}

dependencies {
    implementation(project(":core"))
    implementation(project(":core-ui"))
    implementation(project(":nutrition:nutrition_domain"))

    implementation(libs.coil.compose)
}
