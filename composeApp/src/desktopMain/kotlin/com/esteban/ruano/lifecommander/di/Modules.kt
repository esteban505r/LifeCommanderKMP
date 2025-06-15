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
import com.esteban.ruano.lifecommander.ui.viewmodels.FinanceViewModel
import com.esteban.ruano.lifecommander.services.finance.FinanceService
import com.esteban.ruano.lifecommander.services.meals.RecipesService
import com.esteban.ruano.lifecommander.services.workout.WorkoutService
import com.esteban.ruano.lifecommander.utils.SOCKETS_HOST
import com.esteban.ruano.lifecommander.utils.SOCKETS_PATH
import com.esteban.ruano.lifecommander.utils.SOCKETS_PORT
import com.esteban.ruano.lifecommander.timer.TimerPlaybackManager
import com.esteban.ruano.lifecommander.ui.viewmodels.CategoryKeywordMapperViewModel
import com.esteban.ruano.lifecommander.ui.viewmodels.RecipesViewModel
import com.esteban.ruano.lifecommander.ui.viewmodels.TimersViewModel
import com.esteban.ruano.lifecommander.ui.viewmodels.WorkoutViewModel
import com.esteban.ruano.lifecommander.utils.DEV_VARIANT
import com.esteban.ruano.lifecommander.utils.VARIANT
import com.esteban.ruano.lifecommander.websocket.TimerWebSocketClient
import io.ktor.client.plugins.websocket.WebSockets
import org.koin.core.qualifier.named
import services.dailyjournal.PomodoroService
import services.dashboard.DashboardService
import services.habits.HabitService
import services.tasks.TaskService
import ui.ui.viewmodels.AuthViewModel
import ui.viewmodels.AppViewModel
import ui.viewmodels.DailyJournalViewModel
import ui.viewmodels.DashboardViewModel
import ui.viewmodels.HabitsViewModel
import ui.viewmodels.TasksViewModel
import utils.BackgroundServiceManager
import utils.StatusBarService
import utils.TimerService
import utils.createDataStore


// Network Module
val socketQualifier = named("socketHttpClient")
val networkModule = module {
    single {
        HttpClient(CIO) {
            if(VARIANT == DEV_VARIANT) {
                engine {
                    //Proxy for debugging
                    proxy = ProxyBuilder.http("http://localhost:8000")
                }
            }
            install(ContentNegotiation) {
                gson {
                    setPrettyPrinting()
                }
            }
        }
    }
    single(socketQualifier) {
        HttpClient(CIO) {
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
    single { AuthService(get(),get()) }
    single { FinanceService(BASE_URL, get(), get()) }
    single { AppPreferencesService(get()) }
    single { NightBlockService(get(),get()) }
    single { BackgroundServiceManager() }
    single { TimerPlaybackManager() }
    single { StatusBarService() }
    single { TimerWebSocketClient(get(socketQualifier), SOCKETS_HOST, SOCKETS_PORT, SOCKETS_PATH, get()) }
    single { TimerService(get()) }
    single { com.esteban.ruano.lifecommander.services.timers.TimerService(get()) }
    single { DailyJournalService(BASE_URL, get(), get()) }
    single { CategoryKeywordService(BASE_URL, get(),get()) }
    single { PomodoroService(BASE_URL, get(), get()) }
    single { DashboardService(BASE_URL,get(),get()) }
    single { RecipesService (BASE_URL, get(), get()) }
    single { WorkoutService(BASE_URL, get(), get()) }
}

// ViewModels Module
val viewModelsModule = module {
    viewModel { AppViewModel(get(),get(), get(), get(), get()) }
    viewModel { HabitsViewModel(get(),get()) }
    viewModel { TasksViewModel(get(),get(),get()) }
    viewModel { AuthViewModel(get()) }
    viewModel { DailyJournalViewModel(get(),get()) }
    viewModel { CalendarViewModel(get(),get(),get(),get()) }
    viewModel { FinanceViewModel(get()) }
    viewModel { TimersViewModel(get(),get(), get(),get(),get(),get())}
    viewModel { CategoryKeywordMapperViewModel(get()) }
    viewModel { DashboardViewModel(
        get(),
    ) }
    viewModel { RecipesViewModel(get()) }
    viewModel { WorkoutViewModel(get()) }
}

// Combine all modules
val appModule = module {
    includes(networkModule, managersModule,dataStoreModule, servicesModule, viewModelsModule)
}