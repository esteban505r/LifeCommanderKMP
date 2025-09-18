package com.esteban.ruano.plugins

import com.esteban.ruano.lifecommander.utils.BASE_URL_PROD
import com.esteban.ruano.repository.*
import com.esteban.ruano.service.*
import com.esteban.ruano.utils.EmailSender
import com.esteban.ruano.utils.FirstRunSeeder
import com.esteban.ruano.utils.SesEmailSender
import io.ktor.server.application.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import kotlin.math.sin

fun Application.configureKoin() {
    install(Koin) {
        slf4jLogger()
        modules(appModule)
    }
}

val appModule = module {
    // Services

    single { ReminderService() }

    single<EmailSender> { SesEmailSender() }
    single { PasswordResetService(get(),BASE_URL_PROD,) }
    single { TaskService(get()) }
    single { HabitService(get()) }
    single { NotificationService() }
    single { TimerService() }
    single { TimerCheckerService(get(),get(),get(),get(),get(),get()) }
    single { WorkoutService() }
    single { NutritionService() }
    single { BlogService() }
    single { PostCategoryService() }
    single { PomodoroService() }
    single { QuestionService() }
    single { QuestionAnswerService() }
    single { DailyJournalService(get(), get()) }
    single { AccountService() }
    single { TransactionService() }
    single { BudgetService(get()) }
    single { SavingsGoalService() }
    single { CategoryKeywordService() }
    single { PortfolioService() }
    single { ScheduledTransactionService() }
    single { SyncService(get(), get(), get()) }
    single { SettingsService() }
    single { FirstRunSeeder(get(),get(),get()) }
    single { AuthService(
        get()
    ) }
    
    // Dashboard Service with dependencies
    single { 
        DashboardService(
            taskService = get(),
            habitService = get(),
            transactionService = get(),
            accountService = get(),
            nutritionService = get(),
            workoutService = get(),
            journalService = get()
        ) 
    }

    // Repositories
    single { WorkoutRepository(get()) }
    single { TaskRepository(get()) }
    single { HabitRepository(get()) }
    single { SyncRepository(get()) }
    single { NutritionRepository(get()) }
    single { BlogRepository(get()) }
    single { QuestionRepository(get()) }
    single { QuestionAnswerRepository(get()) }
    single { PomodoroRepository(get()) }
    single { DailyJournalRepository(get()) }
    single { AccountRepository(get()) }
    single { BudgetRepository(get()) }
    single { TransactionRepository(get()) }
    single { SavingsGoalRepository(get()) }
    single { CategoryKeywordRepository(get()) }
    single { PortfolioRepository(get()) }
    single { ScheduledTransactionRepository(get()) }
    single { SettingsRepository(get()) }
} 