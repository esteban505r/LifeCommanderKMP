plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeCompiler)
//    alias(libs.plugins.kotlinAndroid)
}

apply(from = "$rootDir/compose-module.gradle")

android {
    namespace = "com.esteban.ruano.onboarding_presentation"
}

dependencies {
    implementation(project(":core"))
    implementation(project(":core-ui"))
    implementation(project(":onboarding:onboarding_domain"))
    //Firebase - only the classes, not the plugin
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
}
