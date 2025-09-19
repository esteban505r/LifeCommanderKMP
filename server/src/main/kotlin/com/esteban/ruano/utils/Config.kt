package com.esteban.ruano.utils

import software.amazon.awssdk.regions.Region

data class DbConfig(val url: String, val user: String, val password: String)
data class AwsCfg(val region: Region, val sesFrom: String?)
data class AppConfig(
    val db: DbConfig,
    val sentryDsn: String?,
    val aws: AwsCfg,
    val logLevel: String = System.getenv("LOG_LEVEL") ?: "INFO",
    val environment: String = System.getenv("ENVIRONMENT") ?: "production"
)

private fun env(name: String, default: String? = null): String? =
    System.getenv(name) ?: default

fun loadConfig(): AppConfig {
    // Prefer a single JDBC URL if provided
    val jdbcUrl = env("DB_URL") ?: run {
        // Otherwise build it from POSTGRES_* pieces
        val host = env("POSTGRES_HOST", "localhost")!!
        val port = env("POSTGRES_PORT", "5432")!!
        val db   = env("POSTGRES_DB",   "postgres")!!
        val extra = env("DB_PARAMS")?.let { "?$it" } ?: ""
        "jdbc:postgresql://$host:$port/$db$extra"
    }

    // Map user/password from either DB_* or POSTGRES_*
    val dbUser = env("DB_USER") ?: env("POSTGRES_USER")
    ?: error("DB_USER/POSTGRES_USER not set")
    val dbPass = env("DB_PASSWORD") ?: env("POSTGRES_PASSWORD")
    ?: error("DB_PASSWORD/POSTGRES_PASSWORD not set")

    val region = env("AWS_REGION")?.let(Region::of) ?: Region.US_EAST_1
    val sesFrom = env("SES_FROM")
    val sentry = env("SENTRY_DSN") 

    return AppConfig(
        db = DbConfig(jdbcUrl, dbUser, dbPass),
        sentryDsn = sentry,
        aws = AwsCfg(region, sesFrom)
    )
}