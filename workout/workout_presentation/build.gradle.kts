plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeCompiler)
//    alias(libs.plugins.kotlinAndroid)
}

apply(from = "$rootDir/compose-module.gradle")

android {
    namespace = "com.esteban.ruano.workout_presentation"
}

dependencies {
    implementation(project(":core"))
    implementation(project(":core-ui"))
    implementation(project(":workout:workout_domain"))
}
