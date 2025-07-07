plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeCompiler)
}

apply(from = "$rootDir/compose-module.gradle")

android {
    namespace = "com.esteban.ruano.core_ui"
}

dependencies {
    implementation(project(":core"))

    // WorkManager
    api(libs.work.runtime.ktx)

    // Coil
    api(libs.coil.compose)
    api(libs.coil.network)

    // Compose
    api(libs.compose.icons)
    api(libs.compose.ui)
    
    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    
    // Android dependencies for notifications
    implementation(libs.androidx.core.ktx)
//    implementation(libs.androidx.work.runtime.ktx)
  /*  api(platform(libs.compose.bom))

    testApi(platform(libs.compose.bom))
    androidTestApi(platform(libs.compose.bom))
    debugApi(libs.compose.ui.tooling)*/
}
