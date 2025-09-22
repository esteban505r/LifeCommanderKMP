import io.github.cdimascio.dotenv.dotenv
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain
import software.amazon.awssdk.services.rds.RdsClient
import software.amazon.awssdk.services.rds.model.DBInstance
import software.amazon.awssdk.services.rds.model.DescribeDbInstancesRequest


private fun isProduction() = System.getenv("AWS_REGION") != null


private fun buildIamJdbcUrlFromRds(instanceId: String, dbName: String): String {
    val region = DefaultAwsRegionProviderChain.builder().build().region ?: Region.US_EAST_1
    val rds = RdsClient.builder()
        .region(region)
        .credentialsProvider(DefaultCredentialsProvider.builder().build())
        .build()

    // Fetch the DB instance details
    val resp = rds.describeDBInstances(
        DescribeDbInstancesRequest.builder()
            .dbInstanceIdentifier(instanceId)
            .build()
    )

    // Get the endpoint details
    val dbInstance: DBInstance = resp.dbInstances().first()
    val host = dbInstance.endpoint().address()
    val port = dbInstance.endpoint().port()

    // Build the JDBC URL using the IAM wrapper for PostgreSQL
    return "jdbc:aws-wrapper:postgresql://$host:$port/$dbName" +
            "?wrapperPlugins=iam&enableIamAuth=true&sslMode=require"
}

data class DbConfig(val url: String, val user: String, val password: String)
data class AwsCfg(val region: Region, val sesFrom: String?)
data class AppConfig(
    val db: DbConfig,
    val sentryDsn: String = "",
    val aws: AwsCfg,
    val logLevel: String = System.getenv("LOG_LEVEL") ?: "INFO",
    val environment: String = System.getenv("ENVIRONMENT") ?: "production"
)

private fun isIamJdbc(url: String): Boolean {
    val u = url.lowercase()
    return u.startsWith("jdbc:aws-wrapper:postgresql:")
            || "enableiamauth=true" in u
            || "wrapperplugins=iam" in u
}

private fun env(name: String, default: String? = null): String? =
    System.getenv(name) ?: default

fun loadConfig(): AppConfig {
    val dotenv = dotenv { ignoreIfMissing = true }

    fun get(name: String, default: String? = null): String? =
        System.getenv(name) ?: dotenv[name] ?: default

    // Check if running in a production environment (AWS)
    val isProd = isProduction()

    // If in production, use RDS IAM auth; if local, fallback to .env DB config
    val jdbcUrl = if (isProd) {
        // Build the IAM JDBC URL from RDS metadata
        val instanceId = get("RDS_INSTANCE_ID") ?: error("RDS_INSTANCE_ID not set")
        val dbName = get("POSTGRES_DB") ?: "postgres"
        buildIamJdbcUrlFromRds(instanceId, dbName)  // This is the function we defined earlier
    } else {
        // Local dev fallback: Use .env for DB_URL and other variables
        get("DB_URL") ?: run {
            val host = get("POSTGRES_HOST", "localhost")!!
            val port = get("POSTGRES_PORT", "5432")!!
            val db = get("POSTGRES_DB", "postgres")!!
            val extra = get("DB_PARAMS")?.let { if (it.startsWith("?")) it else "?$it" } ?: ""
            "jdbc:postgresql://$host:$port/$db$extra"
        }
    }

    val dbUser = get("DB_USER") ?: get("POSTGRES_USER")
    ?: error("DB_USER/POSTGRES_USER not set")

    val iamMode = isIamJdbc(jdbcUrl)
    val dbPass = if (iamMode) "" else {
        get("DB_PASSWORD") ?: get("POSTGRES_PASSWORD")
        ?: error("DB_PASSWORD/POSTGRES_PASSWORD not set (non-IAM mode)")
    }

    val region = get("AWS_REGION")?.let(Region::of)
        ?: DefaultAwsRegionProviderChain.builder().build().region
        ?: Region.US_EAST_1

    val sesFrom = get("SES_FROM")
    val sentry = get("SENTRY_DSN") ?: ""

    return AppConfig(
        db = DbConfig(jdbcUrl, dbUser, dbPass),
        sentryDsn = sentry,
        aws = AwsCfg(region, sesFrom)
    )
}
