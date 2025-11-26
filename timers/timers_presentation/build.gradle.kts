plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeCompiler)
//    alias(libs.plugins.kotlinAndroid)
}

apply(from = "$rootDir/compose-module.gradle")

android {
    namespace = "com.esteban.ruano.timers_presentation"
}

dependencies {
    implementation(project(":core"))
    implementation(project(":core-ui"))
    implementation(projects.shared)
    implementation(project(":timers:timers_domain"))
    implementation(project(":timers:timers_data"))
    testImplementation(project(":test-core"))
}


