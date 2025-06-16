package com.esteban.ruano

import com.esteban.ruano.database.entities.*
import com.esteban.ruano.database.models.Status
import kotlinx.datetime.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.test.KoinTest
import java.sql.Connection
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

object TestDatabaseConfig {
    private const val TEST_DB_URL = "jdbc:postgresql://localhost:5431/testdb"
    private const val TEST_DB_DRIVER = "org.postgresql.Driver"
    private const val TEST_DB_USER = "testuser"
    private const val TEST_DB_PASSWORD = "testpassword"

    private val allTables = listOf(
        Users, Tasks, TaskTracks, Habits, HabitTracks,
        WorkoutDays, WorkoutTracks, Recipes, DailyJournals,
        Transactions, Accounts, Budgets, SavingsGoals,
        TimerLists, Timers, UserSettings, DeviceTokens,
        CategoryKeywords, Portfolios, Resources, Exercises,
        Equipments, ExercisesWithWorkoutDays, ExercisesWithWorkoutTracks,
        Reminders, Posts, Pomodoros, Questions, QuestionAnswers,
        ScheduledTransactions
    )

    fun init() {
        Database.connect(
            url = TEST_DB_URL,
            driver = TEST_DB_DRIVER,
            user = TEST_DB_USER,
            password = TEST_DB_PASSWORD
        )
        TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
    }

    fun setupTables() {
        transaction {
            SchemaUtils.createMissingTablesAndColumns(*allTables.toTypedArray())
        }
    }

    fun cleanup() {
        transaction {
            // First ensure all tables exist
            SchemaUtils.createMissingTablesAndColumns(*allTables.toTypedArray())
            
            // Then clean up data
            allTables.forEach { table ->
                try {
                    table.deleteAll()
                } catch (e: Exception) {
                    // Log error but continue with other tables
                    println("Error cleaning table ${table.tableName}: ${e.message}")
                }
            }
        }
    }

    fun createTestUser(): Int {
        return transaction {
            val user = User.new {
                name = "Test User"
                email = "test@example.com"
                password = "password"
                status = Status.ACTIVE
            }
            user.id.value
        }
    }

    fun getTestDateTime(offsetDays: Int = 0): LocalDateTime {
        val currentInstant = Clock.System.now()
        return if (offsetDays == 0) {
            currentInstant.toLocalDateTime(TimeZone.currentSystemDefault())
        } else {
            val offsetInstant = currentInstant.plus(offsetDays.toLong(), DateTimeUnit.DAY, TimeZone.currentSystemDefault())
            offsetInstant.toLocalDateTime(TimeZone.currentSystemDefault())
        }
    }
}

abstract class BaseTest: KoinTest {
    protected var userId: Int = 1
    protected val currentDateTime: LocalDateTime
        get() = TestDatabaseConfig.getTestDateTime()

    @BeforeTest
    open fun setup() {
        TestDatabaseConfig.init()
        TestDatabaseConfig.setupTables()
        TestDatabaseConfig.cleanup()
        userId = TestDatabaseConfig.createTestUser()
    }

    @AfterTest
    fun cleanup() {
        TestDatabaseConfig.cleanup()
    }

    protected fun getDateTime(offsetDays: Int = 0): LocalDateTime {
        return TestDatabaseConfig.getTestDateTime(offsetDays)
    }
} 