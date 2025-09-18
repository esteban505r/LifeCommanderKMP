package com.esteban.ruano

import com.esteban.ruano.database.entities.*
import com.esteban.ruano.plugins.configureKoin
import com.esteban.ruano.plugins.configureRouting
import com.esteban.ruano.plugins.configureSecurity
import com.esteban.ruano.plugins.configureSerialization
import com.esteban.ruano.service.TimerCheckerService
import com.esteban.ruano.service.TimerService
import com.esteban.ruano.utils.X_CATEGORY_PASSWORD_HEADER
import com.esteban.ruano.utils.X_POST_PASSWORD_HEADER
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.config.yaml.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.origin
import io.ktor.server.plugins.ratelimit.RateLimit
import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.websocket.*
import io.sentry.Sentry
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.koin.ktor.ext.inject
import org.slf4j.event.Level
import java.util.*
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

private const val MDC_KEY = "requestId"

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

@Suppress("unused")
fun Application.module() {
    configureLogging()
    configureCORS()
    configureWebSockets()
    configureSecurity()
    configureSerialization()
    configureKoin()
    configureRateLimit()
    configureRouting()
    connectToPostgres()

    // Get services from Koin
    val timerService: TimerService by inject()
    val timerCheckerService: TimerCheckerService by inject()

    // Start background tasks
    timerCheckerService.start()
}

fun Application.configureLogging() {
    install(CallId) {
        retrieveFromHeader(HttpHeaders.XRequestId)
        generate { UUID.randomUUID().toString() }
        replyToHeader(HttpHeaders.XRequestId)
    }

    install(CallLogging) {
        level = Level.INFO
        callIdMdc(MDC_KEY)                 // <- surfaces CallId in MDC
        filter { !it.request.path().startsWith("/health") }
        format { call ->
            val status = call.response.status()?.value ?: 0
            "${call.request.httpMethod.value} ${call.request.uri} -> $status"
        }
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->

            if (cause is CancellationException) throw cause

            this@configureLogging.log.error("Unhandled exception", cause)
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "internal_error"))
        }
    }
}
fun Application.configureCORS() {
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        allowMethod(HttpMethod.Get)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Accept)
        allowHeader(HttpHeaders.UserAgent)
        allowHeader(X_POST_PASSWORD_HEADER)
        allowHeader(X_CATEGORY_PASSWORD_HEADER)
        allowHeader("X-Requested-With")
        allowCredentials = true
        allowHost("localhost:3000", schemes = listOf("http", "https"))
        allowHost("127.0.0.1:3000", schemes = listOf("http", "https"))
        allowHost("localhost", schemes = listOf("http", "https"))
        allowHost("estebanruano.com", schemes = listOf("https"))
        allowHost("www.estebanruano.com", schemes = listOf("https"))
    }
}

fun Application.configureRateLimit(){
    install(RateLimit) {
        // Limit password-reset email requests
        register(RateLimitName("forgot")) {
            rateLimiter(limit = 5, refillPeriod = 10.minutes)
            requestKey { call -> call.request.origin.remoteHost } // per-IP
        }
        // Limit actual reset submissions
        register(RateLimitName("reset")) {
            rateLimiter(limit = 10, refillPeriod = 10.minutes)
            requestKey { call -> call.request.origin.remoteHost } // per-IP
        }

        register(RateLimitName("verify")) {
            rateLimiter(limit = 20, refillPeriod = 10.minutes)
            requestKey { call -> call.request.origin.remoteHost }
        }
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
    val sentryDsn = configs?.property("services.postgres.environment.SENTRY_DSN")?.getString()


    Sentry.init { opts ->
        opts.dsn = sentryDsn
        opts.environment = env
        opts.release = "unknown"
    }

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
            ExercisesWithWorkoutTracks, ExerciseSetTracks, Users, Habits,
            Tasks, HistoryTracks, HabitTracks, TaskTracks, Reminders, Recipes, Posts,
            DailyJournals, Pomodoros, Questions, QuestionAnswers,
            Transactions, ScheduledTransactions, Accounts, Budgets, SavingsGoals, TimerLists,
            Timers, UserSettings, DeviceTokens, CategoryKeywords, Portfolios, RecipeTracks, ExerciseTracks, RecipeDays,
            Ingredients, Instructions, PasswordResetPins, PasswordResetSessions, RefreshSessions
        )
    }
}
