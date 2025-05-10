plugins {
    alias(libs.plugins.androidLibrary)
//    alias(libs.plugins.kotlinAndroid)
}

apply(from = "$rootDir/base-module.gradle")

android {
    namespace = "com.esteban.ruano.onboarding_domain"
}

dependencies {
    implementation(project(":core"))
    implementation(project(":core-ui"))
}
