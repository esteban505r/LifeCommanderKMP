package di

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
import services.auth.TokenStorage
import services.dailyjournal.DailyJournalService
import com.esteban.ruano.lifecommander.services.habits.BASE_URL
import com.esteban.ruano.lifecommander.ui.viewmodels.CalendarViewModel
import com.esteban.ruano.lifecommander.ui.viewmodels.FinanceViewModel
import com.esteban.ruano.lifecommander.services.finance.FinanceService
import services.habits.HabitService
import services.tasks.TaskService
import ui.ui.viewmodels.AuthViewModel
import ui.viewmodels.AppViewModel
import ui.viewmodels.DailyJournalViewModel
import ui.viewmodels.HabitsViewModel
import ui.viewmodels.TasksViewModel
import utils.BackgroundServiceManager
import utils.StatusBarService
import utils.TimerService
import utils.createDataStore

// Network Module
val networkModule = module {
    single {
        HttpClient(CIO) {
            engine {
                //Proxy for debugging
                proxy = ProxyBuilder.http("http://localhost:8000")
            }
            install(ContentNegotiation) {
                gson {
                    setPrettyPrinting()
                }
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
    single { TokenStorage(get()) }
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
    single { StatusBarService() }
    single { TimerService(get()) }
    single { DailyJournalService(BASE_URL, get(), get()) }
}

// ViewModels Module
val viewModelsModule = module {
    viewModel { AppViewModel(get(),get(), get(), get(), get(),get()) }
    viewModel { HabitsViewModel(get(),get()) }
    viewModel { TasksViewModel(get(),get(),get()) }
    viewModel { AuthViewModel(get()) }
    viewModel { DailyJournalViewModel(get(),get()) }
    viewModel { CalendarViewModel(get(),get(),get()) }
    viewModel { FinanceViewModel(get()) }
}

// Combine all modules
val appModule = module {
    includes(networkModule, managersModule,dataStoreModule, servicesModule, viewModelsModule)
}