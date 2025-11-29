package di

import com.esteban.ruano.lifecommander.services.finance.CategoryKeywordService
import com.esteban.ruano.lifecommander.utils.TokenStorage
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.gson.*
import io.ktor.client.engine.cio.*
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import services.AppPreferencesService
import services.NightBlockService
import services.auth.AuthService
import services.auth.TokenStorageImpl
import services.dailyjournal.DailyJournalService
import com.esteban.ruano.lifecommander.utils.BASE_URL
import com.esteban.ruano.lifecommander.ui.viewmodels.CalendarViewModel
import com.esteban.ruano.lifecommander.ui.viewmodels.FinanceCoordinatorViewModel
import com.esteban.ruano.lifecommander.ui.viewmodels.AccountViewModel
import com.esteban.ruano.lifecommander.ui.viewmodels.TransactionViewModel
import com.esteban.ruano.lifecommander.ui.viewmodels.BudgetViewModel
import com.esteban.ruano.lifecommander.ui.viewmodels.ScheduledTransactionViewModel
import com.esteban.ruano.lifecommander.services.finance.FinanceService
import com.esteban.ruano.lifecommander.services.meals.RecipesService
import com.esteban.ruano.lifecommander.services.settings.SettingsService
import com.esteban.ruano.lifecommander.services.workout.WorkoutService
import com.esteban.ruano.lifecommander.utils.SOCKETS_HOST
import com.esteban.ruano.lifecommander.utils.SOCKETS_PATH
import com.esteban.ruano.lifecommander.utils.SOCKETS_PORT
import com.esteban.ruano.lifecommander.timer.TimerPlaybackManager
import com.esteban.ruano.lifecommander.ui.viewmodels.CategoryKeywordMapperViewModel
import com.esteban.ruano.lifecommander.ui.viewmodels.FinanceStatisticsViewModel
import com.esteban.ruano.lifecommander.ui.viewmodels.RecipesViewModel
import com.esteban.ruano.lifecommander.ui.viewmodels.SettingsViewModel
import com.esteban.ruano.lifecommander.ui.viewmodels.TimersViewModel
import com.esteban.ruano.lifecommander.ui.viewmodels.WorkoutViewModel
import com.esteban.ruano.lifecommander.utils.DEV_VARIANT
import com.esteban.ruano.lifecommander.utils.VARIANT
import com.esteban.ruano.lifecommander.websocket.TimerWebSocketClient
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.http.HttpHeaders
import org.koin.core.qualifier.named
import services.dailyjournal.PomodoroService
import services.dashboard.DashboardService
import services.habits.HabitService
import services.study.StudyService
import services.tasks.TaskService
import services.tags.TagService
import ui.ui.viewmodels.AuthViewModel
import ui.viewmodels.AppViewModel
import ui.viewmodels.DailyJournalViewModel
import ui.viewmodels.DashboardViewModel
import ui.viewmodels.HabitsViewModel
import ui.viewmodels.TasksViewModel
import ui.viewmodels.TagsViewModel
import com.esteban.ruano.lifecommander.ui.viewmodels.StudyViewModel
import com.esteban.ruano.lifecommander.utils.PROD_VARIANT
import io.ktor.client.request.HttpRequestPipeline
import io.ktor.http.encodedPath
import utils.BackgroundServiceManager
import utils.StatusBarService
import utils.TimerService
import utils.createDataStore
import java.io.FileInputStream
import java.io.InputStream
import java.net.URL
import java.security.KeyStore
import java.security.cert.CertificateFactory
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager


// Network Module
val socketQualifier = named("socketHttpClient")

fun trustManagerFromPem(pemPath: String): X509TrustManager {
    val cf = CertificateFactory.getInstance("X.509")
    val cert = FileInputStream(pemPath).use { cf.generateCertificate(it) }
    val ks = KeyStore.getInstance(KeyStore.getDefaultType()).apply { load(null, null) }
    ks.setCertificateEntry("httptoolkit-root", cert)
    val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
    tmf.init(ks)
    return tmf.trustManagers.single() as X509TrustManager
}

fun trustManagerFromResource(resourcePath: String): X509TrustManager? {
    return try {
        // Try multiple ways to load the resource
        var inputStream: InputStream? = object {}.javaClass.classLoader.getResourceAsStream(resourcePath)
        
        // If that fails, try with leading slash
        if (inputStream == null) {
            inputStream = object {}.javaClass.classLoader.getResourceAsStream("/$resourcePath")
        }
        
        // If still null, try Thread.currentThread().contextClassLoader
        if (inputStream == null) {
            inputStream = Thread.currentThread().contextClassLoader?.getResourceAsStream(resourcePath)
        }
        
        if (inputStream == null) {
            inputStream = Thread.currentThread().contextClassLoader?.getResourceAsStream("/$resourcePath")
        }
        
        if (inputStream == null) {
            println("⚠️ [SSL] Cloudflare certificate not found at resource path: $resourcePath")
            println("⚠️ [SSL] Tried: $resourcePath, /$resourcePath")
            // Debug: list available resources
            try {
                val resources: java.util.Enumeration<URL>? = object {}.javaClass.classLoader.getResources("certs")
                if (resources != null && resources.hasMoreElements()) {
                    println("⚠️ [SSL] Available certs resources:")
                    while (resources.hasMoreElements()) {
                        println("  - ${resources.nextElement()}")
                    }
                } else {
                    println("⚠️ [SSL] No certs resources found")
                }
            } catch (e: Exception) {
                println("⚠️ [SSL] Could not list resources: ${e.message}")
            }
            return null
        }
        
        println("✅ [SSL] Successfully loaded certificate from resource: $resourcePath")
        val cf = CertificateFactory.getInstance("X.509")
        val cert = inputStream.use { cf.generateCertificate(it) }
        
        // Load default system trust store
        val defaultTrustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        defaultTrustManagerFactory.init(null as KeyStore?)
        val defaultTrustManager = defaultTrustManagerFactory.trustManagers.single() as X509TrustManager
        
        // Create a new keystore with default certificates + Cloudflare certificate
        val ks = KeyStore.getInstance(KeyStore.getDefaultType())
        ks.load(null, null)
        
        // Add Cloudflare certificate
        ks.setCertificateEntry("cloudflare-origin", cert)
        
        // Create trust manager with combined certificates
        val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        tmf.init(ks)
        
        // Return a combined trust manager that trusts both default CAs and Cloudflare
        object : X509TrustManager {
            private val cloudflareTrustManager = tmf.trustManagers.single() as X509TrustManager
            
            override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>?, authType: String?) {
                defaultTrustManager.checkClientTrusted(chain, authType)
            }
            
            override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>?, authType: String?) {
                try {
                    // Try default trust manager first
                    defaultTrustManager.checkServerTrusted(chain, authType)
                } catch (e: Exception) {
                    // If default fails, try Cloudflare certificate
                    try {
                        cloudflareTrustManager.checkServerTrusted(chain, authType)
                    } catch (e2: Exception) {
                        throw e // Re-throw original exception if both fail
                    }
                }
            }
            
            override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> {
                return defaultTrustManager.acceptedIssuers
            }
        }
    } catch (e: Exception) {
        println("Warning: Failed to load Cloudflare certificate from resources: ${e.message}")
        null
    }
}

fun createCombinedTrustManager(cloudflareCert: java.security.cert.Certificate?, httptoolkitCert: java.security.cert.Certificate?): X509TrustManager? {
    if (cloudflareCert == null && httptoolkitCert == null) {
        return null
    }
    
    return try {
        // Load default system trust store
        val defaultTrustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        defaultTrustManagerFactory.init(null as KeyStore?)
        val defaultTrustManager = defaultTrustManagerFactory.trustManagers.single() as X509TrustManager
        
        // Create a keystore with both certificates
        val ks = KeyStore.getInstance(KeyStore.getDefaultType())
        ks.load(null, null)
        
        // Add certificates if available
        cloudflareCert?.let { ks.setCertificateEntry("cloudflare-origin", it) }
        httptoolkitCert?.let { ks.setCertificateEntry("httptoolkit", it) }
        
        // Create trust manager with combined certificates
        val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        tmf.init(ks)
        val customTrustManager = tmf.trustManagers.single() as X509TrustManager
        
        // Return a combined trust manager that trusts default CAs, Cloudflare, and HTTP Toolkit
        object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>?, authType: String?) {
                defaultTrustManager.checkClientTrusted(chain, authType)
            }
            
            override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>?, authType: String?) {
                try {
                    // Try default trust manager first
                    defaultTrustManager.checkServerTrusted(chain, authType)
                } catch (e: Exception) {
                    // If default fails, try custom trust manager (Cloudflare + HTTP Toolkit)
                    try {
                        customTrustManager.checkServerTrusted(chain, authType)
                    } catch (e2: Exception) {
                        throw e // Re-throw original exception if both fail
                    }
                }
            }
            
            override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> {
                return defaultTrustManager.acceptedIssuers
            }
        }
    } catch (e: Exception) {
        println("⚠️ [SSL] Failed to create combined trust manager: ${e.message}")
        null
    }
}

fun createTrustManager(): X509TrustManager? {
    return when (VARIANT) {
        DEV_VARIANT -> {
            // Dev mode: load both Cloudflare and HTTP Toolkit certificates
            var cloudflareCert: java.security.cert.Certificate? = null
            var httptoolkitCert: java.security.cert.Certificate? = null
            
            // Try to load Cloudflare certificate
            try {
                val inputStream: InputStream? = object {}.javaClass.classLoader.getResourceAsStream("certs/cloudflare-cert.pem")
                    ?: object {}.javaClass.classLoader.getResourceAsStream("/certs/cloudflare-cert.pem")
                    ?: Thread.currentThread().contextClassLoader?.getResourceAsStream("certs/cloudflare-cert.pem")
                    ?: Thread.currentThread().contextClassLoader?.getResourceAsStream("/certs/cloudflare-cert.pem")
                
                if (inputStream != null) {
                    val cf = CertificateFactory.getInstance("X.509")
                    cloudflareCert = inputStream.use { cf.generateCertificate(it) }
                    println("✅ [SSL] Cloudflare certificate loaded for DEV mode")
                }
            } catch (e: Exception) {
                println("ℹ️ [SSL] Cloudflare certificate not available: ${e.message}")
            }
            
            // Try to load HTTP Toolkit certificate
            try {
                val httptoolkitPath = "${System.getProperty("user.home")}/Downloads/httptoolkit.pem"
                val cf = CertificateFactory.getInstance("X.509")
                httptoolkitCert = FileInputStream(httptoolkitPath).use { cf.generateCertificate(it) }
                println("✅ [SSL] HTTP Toolkit certificate loaded for DEV mode")
            } catch (e: Exception) {
                println("ℹ️ [SSL] HTTP Toolkit certificate not available: ${e.message}")
            }
            
            // Create combined trust manager if at least one certificate is available
            val combinedTm = createCombinedTrustManager(cloudflareCert, httptoolkitCert)
            if (combinedTm != null) {
                val certsLoaded = mutableListOf<String>()
                if (cloudflareCert != null) certsLoaded.add("Cloudflare")
                if (httptoolkitCert != null) certsLoaded.add("HTTP Toolkit")
                println("✅ [SSL] Combined trust manager created with: ${certsLoaded.joinToString(", ")}")
                return combinedTm
            }
            
            // Fallback: try individual trust managers
            if (SOCKETS_PORT == 443 && cloudflareCert != null) {
                // For port 443, prefer Cloudflare if available
                return trustManagerFromResource("certs/cloudflare-cert.pem")
            }
            
            if (httptoolkitCert != null) {
                // Fallback to HTTP Toolkit
                return trustManagerFromPem("${System.getProperty("user.home")}/Downloads/httptoolkit.pem")
            }
            
            println("⚠️ [SSL] No certificates found. SSL connections may fail.")
            println("⚠️ [SSL] Please add at least one of:")
            println("   - cloudflare-cert.pem to composeApp/src/desktopMain/resources/certs/")
            println("   - httptoolkit.pem to ~/Downloads/")
            null
        }
        PROD_VARIANT -> {
            // Prod mode: use Cloudflare certificate
            var tm = trustManagerFromResource("certs/cloudflare-cert.pem")
            if (tm == null) {
                // Try loading from local infra directory as fallback
                try {
                    val projectRoot = System.getProperty("user.dir")
                    val certPath = "$projectRoot/infra/ssl/cloudflare/cert.pem"
                    tm = trustManagerFromPem(certPath)
                    if (tm != null) {
                        println("✅ [SSL] Cloudflare certificate loaded from infra directory")
                        return tm
                    }
                } catch (e: Exception) {
                    println("ℹ️ [SSL] Cloudflare certificate not found in infra directory: ${e.message}")
                }
                println("⚠️ [SSL] Cloudflare certificate not found or failed to load. WebSocket connections may fail.")
                println("⚠️ [SSL] Please add cloudflare-cert.pem to composeApp/src/desktopMain/resources/certs/")
            } else {
                println("✅ [SSL] Cloudflare certificate loaded successfully")
            }
            tm
        }
        else -> null
    }
}

val tm = createTrustManager()

val networkModule = module {
    single {
        HttpClient(CIO) {
            engine {
                if (VARIANT == DEV_VARIANT) {
                    //Proxy for debugging
                    proxy = ProxyBuilder.http("https://localhost:8000")
                }
                // Use custom trust manager if available (for dev HTTP Toolkit or prod Cloudflare cert)
                tm?.let { trustManager ->
                    https {
                        this.trustManager = trustManager
                    }
                    if (VARIANT == PROD_VARIANT) {
                        println("✅ [HTTP] Trust manager configured for HTTP client")
                    }
                } ?: run {
                    if (VARIANT == PROD_VARIANT) {
                        println("⚠️ [HTTP] No trust manager available - certificate may be missing!")
                    }
                }
            }
            install(ContentNegotiation) {
                gson {
                    setPrettyPrinting()
                }
            }
            install(ContentEncoding) {
                gzip()
                deflate()
                identity()
            }
        }
    }
    single(socketQualifier) {
        HttpClient(CIO) {
            engine {
                // Only configure SSL trust manager for SSL connections (port 443 or PROD_VARIANT)
                // For non-SSL connections (ws://), don't configure trust manager
                if (VARIANT == PROD_VARIANT || SOCKETS_PORT == 443) {
                    tm?.let { trustManager ->
                        https {
                            this.trustManager = trustManager
                        }
                        println("✅ [WebSocket] Trust manager configured for WebSocket client (SSL)")
                    } ?: run {
                        if (VARIANT == PROD_VARIANT) {
                            println("⚠️ [WebSocket] No trust manager available - certificate may be missing!")
                        }
                    }
                } else {
                    println("ℹ️ [WebSocket] Using non-SSL WebSocket connection (ws://) - no trust manager needed")
                }
            }
            install(ContentNegotiation) {
                gson {
                    setPrettyPrinting()
                }
            }
            install(WebSockets)
        }.also { client ->
            // Add request logging interceptor after client creation
            client.requestPipeline.intercept(HttpRequestPipeline.State) {
                val request = context
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                println("🔌 [Web Socket] Request Details:")
                println("  Method: ${request.method}")
                println("  URL: ${request.url}")
                println("  Protocol: ${request.url.protocol}")
                println("  Host: ${request.url.host}")
                println("  Port: ${request.url.port}")
                println("  Path: ${request.url.encodedPath}")
                println("  Full URL: ${request.url.buildString()}")
                println("  Headers:")
                request.headers.entries().forEach { header ->
                    val headerName = header.key
                    val headerValue = header.value.firstOrNull() ?: ""
                    if (headerName == HttpHeaders.Authorization) {
                        // Mask authorization token for security
                        val maskedValue = if (headerValue.length > 20) {
                            "${headerValue.take(20)}...${headerValue.takeLast(10)}"
                        } else {
                            "***masked***"
                        }
                        println("    $headerName: $maskedValue")
                    } else {
                        println("    $headerName: ${header.value.joinToString(", ")}")
                    }
                }
                println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            }
        }
    }
}


// Data Store Module
val dataStoreModule = module {
    single { createDataStore() }
}

// Managers
val managersModule = module {
    single<TokenStorage> { TokenStorageImpl(get()) }
    single { TokenStorageImpl(get()) }
}

// Services Module
val servicesModule = module {
    single { HabitService(get()) }
    single { TaskService(get()) }
    single { TagService(get()) }
    single { AuthService(get(), get()) }
    single { FinanceService(BASE_URL, get(), get()) }
    single { AppPreferencesService(get()) }
    single { NightBlockService(get()) }
    single { BackgroundServiceManager() }
    single { TimerPlaybackManager() }
    single { StatusBarService() }
    single { TimerWebSocketClient(get(socketQualifier), SOCKETS_HOST, SOCKETS_PORT, SOCKETS_PATH, get()) }
    single { TimerService(get()) }
    single { com.esteban.ruano.lifecommander.services.timers.TimerService(get()) }
    single { DailyJournalService(BASE_URL, get(), get()) }
    single { CategoryKeywordService(BASE_URL, get(), get()) }
    single { PomodoroService(BASE_URL, get(), get()) }
    single { DashboardService(BASE_URL, get(), get()) }
    single { RecipesService(BASE_URL, get(), get()) }
    single { WorkoutService(BASE_URL, get(), get()) }
    single { SettingsService(BASE_URL, get(), get()) }
    single { StudyService(BASE_URL, get(), get()) }
}

// ViewModels Module
val viewModelsModule = module {
    viewModel { AppViewModel(get(), get(), get(), get(), get()) }
    viewModel { HabitsViewModel(get(), get()) }
    viewModel { TasksViewModel(get(), get(), get()) }
    viewModel { TagsViewModel(get(), get()) }
    viewModel { AuthViewModel(get()) }
    viewModel { DailyJournalViewModel(get(), get()) }
    viewModel { CalendarViewModel(get(), get(), get(), get()) }
    viewModel { FinanceCoordinatorViewModel() }
    viewModel { AccountViewModel(get()) }
    viewModel { TransactionViewModel(get()) }
    viewModel { BudgetViewModel(get()) }
        viewModel { ScheduledTransactionViewModel(get()) }
        viewModel { FinanceStatisticsViewModel(get()) }
        viewModel { TimersViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { CategoryKeywordMapperViewModel(get()) }
    viewModel {
        DashboardViewModel(
            get(),
        )
    }
    viewModel { RecipesViewModel(get()) }
    viewModel { WorkoutViewModel(get()) }
    viewModel { SettingsViewModel(get()) }
    viewModel { StudyViewModel(get(), get()) }
}

// Combine all modules
val appModule = module {
    includes(networkModule, managersModule, dataStoreModule, servicesModule, viewModelsModule)
}