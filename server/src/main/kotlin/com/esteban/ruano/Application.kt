package com.esteban.ruano

import io.ktor.server.application.*
import io.ktor.server.config.yaml.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.esteban.ruano.database.entities.*
import com.esteban.ruano.plugins.*
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureSecurity()
    configureSerialization()
    configureRouting()
    connectToPostgres()
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
            Resources,WorkoutTracks,Exercises,
            Equipments,WorkoutDays,ExercisesWithWorkoutDays,
            ExercisesWithWorkoutTracks,Users, Habits,
            Tasks, HistoryTracks, HabitTracks, Reminders, Recipes, Posts,
            DailyJournals, Pomodoros, Questions, QuestionAnswers
        )
    }

}
