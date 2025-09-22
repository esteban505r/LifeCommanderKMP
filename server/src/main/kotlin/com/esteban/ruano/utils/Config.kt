package com.esteban.ruano.utils

import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import io.github.cdimascio.dotenv.dotenv
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest


data class DbConfig(val url: String, val user: String, val password: String)
data class AwsCfg(val region: Region, val sesFrom: String?)
data class AppConfig(
    val db: DbConfig,
    val sentryDsn: String = "",
    val aws: AwsCfg,
    val logLevel: String = System.getenv("LOG_LEVEL") ?: "INFO",
    val environment: String = System.getenv("ENVIRONMENT") ?: "production"
)

/** Detect if JDBC URL is using AWS IAM auth (no password) */
private fun isIamJdbc(url: String): Boolean {
    val u = url.lowercase()
    return u.startsWith("jdbc:aws-wrapper:postgresql:") ||
            "enableiamauth=true" in u || "wrapperplugins=iam" in u
}

private fun envOr(map: Map<String, String>, dot: io.github.cdimascio.dotenv.Dotenv, name: String, default: String? = null): String? =
    map[name] ?: System.getenv(name) ?: dot[name] ?: default

fun loadConfig(): AppConfig {
    val dotenv = dotenv { ignoreIfMissing = true }

    // If set, pull a JSON secret (e.g., {"DB_URL": "...", "DB_USER": "...", ...})
    val secretId = System.getenv("APP_SECRET_ID") ?: dotenv["APP_SECRET_ID"]
    val secretMap = try {
        if (secretId != null) loadSecretMap(secretId) else emptyMap()
    } catch (_: Exception) { emptyMap() }

    fun get(name: String, default: String? = null) = envOr(secretMap, dotenv, name, default)

    // JDBC URL: prefer DB_URL, otherwise build one (non-IAM default)
    val jdbcUrl = get("DB_URL") ?: run {
        val host = get("POSTGRES_HOST", "localhost")!!
        val port = get("POSTGRES_PORT", "5432")!!
        val db   = get("POSTGRES_DB",   "postgres")!!
        val extra = get("DB_PARAMS")?.let { if (it.startsWith("?")) it else "?$it" } ?: ""
        "jdbc:postgresql://$host:$port/$db$extra"
    }

    val iamMode = isIamJdbc(jdbcUrl)

    val dbUser = get("DB_USER") ?: get("POSTGRES_USER")
    ?: error("DB_USER/POSTGRES_USER not set")

    // In IAM mode we intentionally allow an empty password
    val dbPass = if (iamMode) "" else {
        get("DB_PASSWORD") ?: get("POSTGRES_PASSWORD")
        ?: error("DB_PASSWORD/POSTGRES_PASSWORD not set (non-IAM mode)")
    }

    val region = get("AWS_REGION")?.let(Region::of)
        ?: DefaultAwsRegionProviderChain.builder().build().region
        ?: Region.US_EAST_1

    val sesFrom = get("SES_FROM")
    val sentry  = get("SENTRY_DSN") ?: ""

    return AppConfig(
        db = DbConfig(jdbcUrl, dbUser, dbPass),
        sentryDsn = sentry,
        aws = AwsCfg(region, sesFrom)
    )
}

fun loadSecretMap(secretId: String): Map<String, String> {
    val region = DefaultAwsRegionProviderChain.builder().build().region
        ?: Region.US_EAST_1

    val sm = SecretsManagerClient.builder()
        .credentialsProvider(DefaultCredentialsProvider.builder().build())
        .region(region)
        .build()

    val res = sm.getSecretValue(
        GetSecretValueRequest.builder().secretId(secretId).build()
    )

    val json = res.secretString()
    val gson = Gson()
    val type = object : TypeToken<Map<String, String>>() {}.type
    return gson.fromJson(json, type)
}