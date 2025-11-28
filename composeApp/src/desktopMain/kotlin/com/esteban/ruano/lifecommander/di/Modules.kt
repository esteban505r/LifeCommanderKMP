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
import utils.BackgroundServiceManager
import utils.StatusBarService
import utils.TimerService
import utils.createDataStore
import java.io.FileInputStream
import java.io.InputStream
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
        val inputStream: InputStream? = object {}.javaClass.classLoader.getResourceAsStream(resourcePath)
        if (inputStream == null) {
            println("Warning: Cloudflare certificate not found at $resourcePath")
            return null
        }
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

fun createTrustManager(): X509TrustManager? {
    return when (VARIANT) {
        DEV_VARIANT -> {
            // Dev mode: use HTTP Toolkit certificate
            try {
                trustManagerFromPem("${System.getProperty("user.home")}/Downloads/httptoolkit.pem")
            } catch (e: Exception) {
                println("Warning: HTTP Toolkit certificate not found, using default trust manager")
                null
            }
        }
        PROD_VARIANT -> {
            // Prod mode: use Cloudflare certificate
            val tm = trustManagerFromResource("certs/cloudflare-cert.pem")
            if (tm == null) {
                println("⚠️ [SSL] Cloudflare certificate not found or failed to load. WebSocket connections may fail.")
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