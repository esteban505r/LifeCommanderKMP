plugins {
    alias(libs.plugins.androidLibrary)
    kotlin("kapt")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22"
}

apply(from = "$rootDir/base-module.gradle")

android {
    namespace = "com.esteban.ruano.core_data"
}

dependencies {
    implementation(project(":core"))

    // WorkManager
    api(libs.work.runtime.ktx)
    api(libs.work.testing)

    // Retrofit & Serialization
    api(libs.okhttp)
    api(libs.retrofit)
    api(libs.okhttp.logging.interceptor)
    api(libs.retrofit.gson)
    api(libs.retrofit.kotlinx.serialization)
    api(libs.kotlinx.serialization.json)

    // Hilt Testing
//    api(libs.hilt.testing)

    // Room
    "kapt"(libs.room.compiler)
    api(libs.room.ktx)
    api(libs.room.runtime)
}
