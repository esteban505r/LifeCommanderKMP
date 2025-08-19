plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.sentry)
    application
}

group = "com.esteban.ruano"
version = "1.0.0"

application {
    mainClass.set("com.esteban.ruano.ApplicationKt")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=${extra["io.ktor.development"] ?: "false"}")
}

repositories {
    mavenCentral()
    google() // Required for Firebase Admin SDK
}

dependencies {
    implementation(projects.shared)
    implementation(libs.logback)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.websockets)
    implementation(libs.ktor.server.call.logging)
    implementation("io.ktor:ktor-server-core:3.2.3")
    implementation("io.ktor:ktor-server-host-common:3.2.3")
    implementation("io.ktor:ktor-server-core:3.2.3")
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.serialization.gson.jvm)
    implementation(libs.ktor.server.config.yaml)
    implementation(libs.ktor.server.netty)

    implementation(libs.koin.core)
    implementation(libs.koin.ktor)
    implementation(libs.koin.logger.slf4j)

    testImplementation(libs.koin.testing)
    testImplementation(libs.koin.testing.junit)

    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.kotlin.datetime)
    implementation(libs.kotlinx.datetime)
    implementation(libs.exposed.json)
    implementation(libs.ktor.cors)

    implementation(libs.flyway.core)
    implementation(libs.flyway.postgres)

    implementation(libs.bcrypt)
    implementation(libs.postgresql)
    implementation(libs.logback.classic)

    implementation(platform(libs.aws.bom))
    implementation(libs.aws.s3)

    implementation(libs.dotenv.kotlin)
    
    // Firebase Admin SDK for FCM notifications
    implementation(libs.firebase.admin)
    implementation(libs.sentry)
    implementation(libs.call.id)
    implementation(libs.status.pages)
}

// Configure shadow plugin for ZIP64 support
tasks.shadowJar {
    isZip64 = true
}

sentry {
    includeSourceContext = true

    org = "personal-tow"
    projectName = "kotlin"
    authToken = providers.gradleProperty("sentry.auth.token").orNull
}