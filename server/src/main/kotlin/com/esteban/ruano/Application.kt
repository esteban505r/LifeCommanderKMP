package com.esteban.ruano

import com.esteban.ruano.database.entities.*
import com.esteban.ruano.plugins.*
import com.esteban.ruano.service.TimerCheckerService
import com.esteban.ruano.service.TimerService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.config.yaml.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import io.ktor.server.websocket.*
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.ktor.ext.inject
import kotlin.time.Duration.Companion.seconds

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

@Suppress("unused")
fun Application.module() {
    configureCORS()
    configureWebSockets()
    configureSecurity()
    configureSerialization()
    configureKoin()
    configureRouting()
    connectToPostgres()

/*    // Get services from Koin
    val timerService: TimerService by inject()
    val timerCheckerService: TimerCheckerService by inject()*/

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
        allowHeader(HttpHeaders.Authorization)
        allowHost("localhost:3000", schemes = listOf("http", "https"))
    }
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
        throw Exception("No user or password detected")
    }

    Flyway
        .configure()
        .baselineOnMigrate(true)
        .validateMigrationNaming(true)
        .dataSource(
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
            Tasks, HistoryTracks, HabitTracks, TaskTracks, Reminders, Recipes, Posts,
            DailyJournals, Pomodoros, Questions, QuestionAnswers,
            Transactions, ScheduledTransactions, Accounts, Budgets, SavingsGoals, TimerLists,
            Timers, UserSettings, DeviceTokens, CategoryKeywords, Portfolios, RecipeTracks
        )
    }
}
