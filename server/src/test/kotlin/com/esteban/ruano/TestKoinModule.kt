package com.esteban.ruano

import com.esteban.ruano.repository.*
import com.esteban.ruano.service.*
import org.koin.dsl.module

val testModule = module {
    single { ReminderService() }
    single { AuthService() }
    single { TaskService(get()) }
    single { HabitService(get()) }
    single { TimerService() }
    single { TimerCheckerService(get()) }
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
    single { ScheduledTransactionRepository(get()) }
    single { PortfolioRepository(get()) }
} 