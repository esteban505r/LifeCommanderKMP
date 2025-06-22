plugins {
    alias(libs.plugins.androidLibrary)
//    alias(libs.plugins.kotlinAndroid)
}

apply(from = "$rootDir/base-module.gradle")

android {
    namespace = "com.esteban.ruano.nutrition_domain"
}

dependencies {
    implementation(project(":core"))
    implementation(project(":shared"))
    implementation(libs.kotlinx.coroutines.core)
}
