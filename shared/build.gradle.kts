import com.android.build.api.dsl.LibraryExtension

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.moko.resources)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.composeMultiplatform)
}

kotlin {
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
        }
    }
    
    jvm()

    val includeFrontend = (
            System.getenv("INCLUDE_ANDROID")
                ?: System.getProperty("includeAndroid")
                ?: "true"
            ).toBoolean()

    if (includeFrontend) {
        apply(plugin =  libs.plugins.androidLibrary.get().pluginId)
        androidTarget()
    }
    
    /*@OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            val rootDirPath = project.rootDir.path
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(rootDirPath)
                        add(projectDirPath)
                    }
                }
            }
        }
    }*/
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.datetime)
                implementation(libs.ktor.client.core)
                implementation(libs.datastore)
                implementation(libs.datastore.preferences)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)

//                implementation(libs.paging)
                implementation(libs.kotlinx.serialization.json)
                implementation(compose.runtime)
                implementation(libs.compose.lifecycle)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.material3)
                api(libs.moko.resources)
                api(libs.moko.resources.compose)
                implementation(compose.materialIconsExtended)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.components.resources)

            }
        }
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
        }
    }
}

val includeFrontend = (
        System.getenv("INCLUDE_ANDROID")
            ?: System.getProperty("includeAndroid")
            ?: "true"
        ).toBoolean()

if(includeFrontend){
    extensions.configure<LibraryExtension>("android") {
        namespace = "com.esteban.ruano.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }
        defaultConfig {
            minSdk = libs.versions.android.minSdk.get().toInt()
        }
    }

}

compose {
    resources{
        publicResClass = true
        packageOfResClass = "com.esteban.ruano.resources"
        generateResClass = always
    }
}

multiplatformResources {
    resourcesPackage.set("com.esteban.ruano")
}


