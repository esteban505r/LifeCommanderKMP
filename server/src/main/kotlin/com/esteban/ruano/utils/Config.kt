import io.github.cdimascio.dotenv.dotenv
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain
import software.amazon.awssdk.services.rds.RdsClient
import software.amazon.awssdk.services.rds.model.DescribeDbInstancesRequest

// Check if running in AWS (production)
private fun isProduction() = false

// Generate IAM-based JDBC URL
private fun buildIamJdbcUrlFromRds(instanceId: String, dbName: String): String {
    val region = DefaultAwsRegionProviderChain.builder().build().region ?: Region.US_EAST_1
    val rdsClient = RdsClient.builder()
        .region(region)
        .credentialsProvider(DefaultCredentialsProvider.create())
        .build()

    // Fetch DB instance details
    val describeRequest = DescribeDbInstancesRequest.builder()
        .dbInstanceIdentifier(instanceId)
        .build()

    val dbInstance = rdsClient.describeDBInstances(describeRequest).dbInstances().first()

    // Extract host and port
    val host = dbInstance.endpoint().address()
    val port = dbInstance.endpoint().port()

    // Return IAM-based JDBC URL
    return "jdbc:aws-wrapper:postgresql://$host:$port/$dbName?wrapperPlugins=iam&enableIamAuth=true&sslMode=require"
}

// Data classes for configuration
data class DbConfig(val url: String, val user: String, val password: String)
data class AwsCfg(val region: Region, val sesFrom: String?)
data class AppConfig(
    val db: DbConfig,
    val sentryDsn: String = "",
    val aws: AwsCfg,
    val logLevel: String = System.getenv("LOG_LEVEL") ?: "INFO",
    val environment: String = System.getenv("ENVIRONMENT") ?: "production"
)

// Check if the JDBC URL is IAM-based (passwordless authentication)
private fun isIamJdbc(url: String): Boolean {
    val u = url.lowercase()
    return u.startsWith("jdbc:aws-wrapper:postgresql:") || "enableiamauth=true" in u || "wrapperplugins=iam" in u
}

// Function to fetch environment variables or dotenv values
private fun envOr(map: Map<String, String>, dot: io.github.cdimascio.dotenv.Dotenv, name: String, default: String? = null): String? =
    map[name] ?: System.getenv(name) ?: dot[name] ?: default

fun loadConfig(): AppConfig {
    val dotenv = dotenv { ignoreIfMissing = true }

    // Fetch values from environment or dotenv
    fun get(name: String, default: String? = null): String? =
        envOr(emptyMap(), dotenv, name, default)

    // Check if in production (AWS), otherwise fallback to local settings
    val isProd = isProduction()

    // Build the JDBC URL dynamically depending on the environment
    val jdbcUrl = if (isProd) {
        // Fetch RDS instance ID and build IAM JDBC URL
        val instanceId = get("RDS_INSTANCE_ID") ?: error("RDS_INSTANCE_ID not set")
        val dbName = get("POSTGRES_DB") ?: "postgres"
        buildIamJdbcUrlFromRds(instanceId, dbName)  // Build IAM-based URL from RDS metadata
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

    // Check if IAM is used
    val iamMode = isIamJdbc(jdbcUrl)
    val dbPass = if (iamMode) "" else {
        get("DB_PASSWORD") ?: get("POSTGRES_PASSWORD")
        ?: error("DB_PASSWORD/POSTGRES_PASSWORD not set (non-IAM mode)")
    }

    // Set AWS region
    val region = get("AWS_REGION")?.let(Region::of)
        ?: DefaultAwsRegionProviderChain.builder().build().region
        ?: Region.US_EAST_1

    // Fetch additional AWS configurations
    val sesFrom = get("SES_FROM")
    val sentry = get("SENTRY_DSN") ?: ""

    return AppConfig(
        db = DbConfig(jdbcUrl, dbUser, dbPass),
        sentryDsn = sentry,
        aws = AwsCfg(region, sesFrom)
    )
}
