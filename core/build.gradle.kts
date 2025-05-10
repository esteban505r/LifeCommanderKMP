plugins {
    alias(libs.plugins.androidLibrary)
}

apply(from = "$rootDir/base-module.gradle")

android {
    namespace = "com.esteban.ruano.core"
}

dependencies   {
    api(libs.datastore.preferences)
    implementation(libs.work.runtime.ktx)
}
