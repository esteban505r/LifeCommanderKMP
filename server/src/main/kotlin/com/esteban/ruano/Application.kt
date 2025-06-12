package com.esteban.ruano

import com.esteban.ruano.database.entities.*
import com.esteban.ruano.plugins.*
import com.esteban.ruano.service.TimerCheckerService
import com.esteban.ruano.service.TimerService
import com.esteban.ruano.utils.X_POST_PASSWORD_HEADER
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.config.yaml.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import io.ktor.server.websocket.*
import io.ktor.server.request.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.application.ApplicationCallPipeline
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.time.Duration.Companion.seconds
import org.slf4j.event.Level

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

@Suppress("unused")
fun Application.module() {
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/api") }
    }
    configureCORS()
    configureWebSockets()
    configureSecurity()
    configureSerialization()
    configureRouting()
    connectToPostgres()

    // Initialize services
    val timerService = TimerService()
    val timerCheckerService = TimerCheckerService(timerService)

    // Start background tasks
//    timerCheckerService.start()
}

fun Application.configureCORS() {
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Get)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Accept)
        allowHeader(HttpHeaders.UserAgent)
        allowHeader(X_POST_PASSWORD_HEADER)
        allowHeader("X-Requested-With")
        allowCredentials = true
        allowHost("localhost:3000", schemes = listOf("http", "https"))
        allowHost("127.0.0.1:3000", schemes = listOf("http", "https"))
        

    }
    
    // Add interceptor to log request headers for debugging
    /* intercept(ApplicationCallPipeline.Call) { 
        if (call.request.path().contains("/api/v1/public/portfolio")) {
            application.log.info("=== CORS DEBUG for ${call.request.path()} ===")
            application.log.info("Request method: ${call.request.httpMethod.value}")
            application.log.info("Request headers:")
            call.request.headers.forEach { name, values ->
                application.log.info("  $name: ${values.joinToString(", ")}")
            }
            application.log.info("Origin: ${call.request.headers[HttpHeaders.Origin]}")
            application.log.info("=== END CORS DEBUG ===")
        }
    } */
}

fun Application.configureWebSockets() {
    install(WebSockets) {
        pingPeriod = 15.seconds
        timeout = 15.seconds
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
}

fun Application.connectToPostgres() {
    val configs = YamlConfig("postgres.yaml")
    val db = configs?.property("services.postgres.environment.POSTGRES_DB")?.getString()
    val user = configs?.property("services.postgres.environment.POSTGRES_USER")?.getString()
    val password = configs?.property("services.postgres.environment.POSTGRES_PASSWORD")?.getString()
    val env = configs?.property("services.postgres.environment.POSTGRES_ENV")?.getString()
    val host = configs?.property("services.postgres.environment.POSTGRES_HOST")?.getString()


    if (user.isNullOrEmpty() || password.isNullOrEmpty()) {
        throw Exception("No user or password detected");
    }


    Flyway
        .configure()
        .baselineOnMigrate(true).validateMigrationNaming(true).dataSource(
            "jdbc:postgresql://$host:5432/$db",
            user,
            password
        ).load().migrate()


    Database.connect(
        "jdbc:postgresql://$host:5432/$db",
        driver = "org.postgresql.Driver",
        user = user,
        password = password
    )

    transaction {
        SchemaUtils.createMissingTablesAndColumns(
            Resources, WorkoutTracks, Exercises,
            Equipments, WorkoutDays, ExercisesWithWorkoutDays,
            ExercisesWithWorkoutTracks, Users, Habits,
            Tasks, HistoryTracks, HabitTracks, Reminders, Recipes, Posts, PostCategories,
            DailyJournals, Pomodoros, Questions, QuestionAnswers,
            Transactions, ScheduledTransactions, Accounts, Budgets, SavingsGoals, TimerLists,
            Timers, UserSettings, DeviceTokens, CategoryKeywords, Portfolios
        )
    }

}
