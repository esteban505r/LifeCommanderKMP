plugins {
    alias(libs.plugins.androidLibrary)
//    alias(libs.plugins.kotlinAndroid)
    kotlin("kapt")
}

apply(from = "$rootDir/base-module.gradle")

android {
    namespace = "com.esteban.ruano.nutrition_data"
}

dependencies {
    implementation(project(":core"))
    implementation(project(":core-data"))
    implementation(project(":nutrition:nutrition_domain"))

    implementation(libs.kotlinx.coroutines.core)

    "kapt"(libs.room.compiler)
    implementation(libs.room.ktx)
    implementation(libs.room.runtime)
}
