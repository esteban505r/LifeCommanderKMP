package com.esteban.ruano.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.esteban.ruano.repository.*
import com.esteban.ruano.routing.*
import com.esteban.ruano.routing.habitsRouting
import com.esteban.ruano.service.*
import com.esteban.ruano.utils.VERSION
import com.esteban.ruano.routing.portfolioRouting
import com.esteban.ruano.service.DashboardService
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties
import org.koin.ktor.ext.inject


inline fun <reified T : Any> Application.validateParameters(parameters: Parameters): String? {
    val dtoClass = T::class
    val expectedParameters = dtoClass.memberProperties.associateBy({ it.name }, { it.returnType.classifier as KClass<*> })

    for ((key, type) in expectedParameters) {
        val value = parameters[key] ?: return "Missing parameter: $key"
        when (type) {
            Int::class -> if (value.toIntOrNull() == null) return "Invalid integer parameter: $key"
            String::class -> {} // Strings are always valid
            // Add more types as needed
            else -> throw IllegalArgumentException("Unsupported parameter type: $type")
        }
    }
    return null
}

fun Application.configureRouting() {
    val habitRepository: HabitRepository by inject()
    val taskRepository: TaskRepository by inject()
    val workoutRepository: WorkoutRepository by inject()
    val syncRepository: SyncRepository by inject()
    val nutritionRepository: NutritionRepository by inject()
    val blogRepository: BlogRepository by inject()
    val questionRepository: QuestionRepository by inject()
    val questionAnswerRepository: QuestionAnswerRepository by inject()
    val pomodoroRepository: PomodoroRepository by inject()
    val dailyJournalRepository: DailyJournalRepository by inject()
    val accountRepository: AccountRepository by inject()
    val budgetRepository: BudgetRepository by inject()
    val transactionRepository: TransactionRepository by inject()
    val savingsGoalRepository: SavingsGoalRepository by inject()
    val categoryKeywordRepository: CategoryKeywordRepository by inject()
    val scheduledTransactionRepository: ScheduledTransactionRepository by inject()
    val portfolioRepository: PortfolioRepository by inject()
    val settingsRepository: SettingsRepository by inject()
    
    // Inject services
    val authService: AuthService by inject()
    val passwordResetService: PasswordResetService by inject()
    val timerService: TimerService by inject()
    val postCategoryService: PostCategoryService by inject()
    val dashboardService: DashboardService by inject()
    val notificationService: NotificationService by inject()


    routing {
        get("/") {
            call.respondText("Hello, world!")
        }

        routing { get("/health") { call.respondText("OK") } }


        route("/api/$VERSION") {
            authenticate {
                habitsRouting(habitRepository)
                tasksRouting(taskRepository)
                workoutRouting(workoutRepository)
                syncRouting(syncRepository)
                nutritionRouting(nutritionRepository)
                questionRouting(questionRepository)
                questionAnswerRouting(questionAnswerRepository)
                pomodoroRouting(pomodoroRepository)
                dailyJournalRouting(dailyJournalRepository)
                timerRouting(timerService)
                settingsRouting(settingsRepository, notificationService, timerService)
                financeRouting(
                    accountRepository = accountRepository,
                    transactionRepository = transactionRepository,
                    budgetRepository = budgetRepository,
                    savingsGoalRepository = savingsGoalRepository,
                    categoryKeywordRepository = categoryKeywordRepository,
                    scheduledTransactionRepository = scheduledTransactionRepository,
                )
            }

            blogRouting(blogRepository, postCategoryService)
            portfolioRouting(portfolioRepository)
            authRouting(authService, passwordResetService,timerService)
            dashboardRouting(dashboardService)
        }
    }
}
