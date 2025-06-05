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
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties


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
    val habitService = HabitService(
        ReminderService()
    )
    val authService = AuthService()
    val workoutService = WorkoutService()
    val taskService = TaskService(
        ReminderService()
    )
    val syncService = SyncService(
        taskService,
        habitService,
        workoutService
    )
    val nutritionService = NutritionService()
    val blogService = BlogService()
    val timerService = TimerService()

    val workoutRepository = WorkoutRepository(workoutService)
    val taskRepository = TaskRepository(taskService)
    val habitRepository = HabitRepository(habitService)
    val syncRepository = SyncRepository(syncService)
    val nutritionRepository = NutritionRepository(nutritionService)
    val blogRepository = BlogRepository(blogService)
    val questionRepository = QuestionRepository(QuestionService())
    val questionAnswerRepository = QuestionAnswerRepository(QuestionAnswerService())
    val pomodoroRepository = PomodoroRepository(PomodoroService())
    val dailyJournalRepository = DailyJournalRepository(DailyJournalService(PomodoroService(), QuestionAnswerService()))
    val accountRepository = AccountRepository(AccountService())
    val budgetRepository = BudgetRepository(BudgetService())
    val transactionRepository = TransactionRepository(TransactionService())
    val savingsGoalRepository = SavingsGoalRepository(SavingsGoalService())
    val categoryKeywordRepository = CategoryKeywordRepository(CategoryKeywordService())
    val scheduledTransactionRepository = ScheduledTransactionRepository(ScheduledTransactionService())
    val portfolioRepository = PortfolioRepository(PortfolioService())

    routing {
        // Handle preflight OPTIONS requests globally
        options("/{...}") {
            call.respond(HttpStatusCode.OK)
        }
        
        get("/") {
            call.respondText("Hello, world!")
        }

        route("/api/$VERSION") {
            // Handle preflight OPTIONS requests for API routes
            options("/{...}") {
                call.respond(HttpStatusCode.OK)
            }
            
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

                financeRouting(
                    accountRepository = accountRepository,
                    transactionRepository = transactionRepository,
                    budgetRepository = budgetRepository,
                    savingsGoalRepository = savingsGoalRepository,
                    categoryKeywordRepository = categoryKeywordRepository,
                    scheduledTransactionRepository = scheduledTransactionRepository
                )
            }

            blogRouting(blogRepository)

            portfolioRouting(portfolioRepository)


            authRouting(authService)

        }
    }
}
