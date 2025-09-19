package com.lifecommander.finance.server

import com.esteban.ruano.database.entities.Accounts
import com.esteban.ruano.database.entities.Budgets
import com.esteban.ruano.database.entities.HistoryTracks
import com.esteban.ruano.database.entities.Transactions
import com.esteban.ruano.database.entities.Users
import com.esteban.ruano.repository.SettingsRepository
import com.esteban.ruano.service.BudgetService
import com.esteban.ruano.service.SettingsService
import com.esteban.ruano.service.TransactionService
import com.esteban.ruano.utils.DateUIUtils.formatDefault
import com.esteban.ruano.utils.DateUtils.formatDateTime
import com.lifecommander.finance.model.AccountType
import com.lifecommander.finance.model.TransactionType
import com.lifecommander.models.Frequency
import kotlin.time.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.junit.Before
import java.sql.Connection
import java.util.UUID
import kotlin.collections.get
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime

class BudgetDBTest{
    private lateinit var service: BudgetService
    private lateinit var transactionService : TransactionService

    @Before
    fun setup() {
        service = BudgetService(
            settingsRepository = SettingsRepository(
                SettingsService()
            )
        )
        transactionService = TransactionService()

        Database.connect(
            url = "jdbc:postgresql://localhost:5431/testdb",
            driver = "org.postgresql.Driver",
            user = "testuser",
            password = "testpassword"
        )
        TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE

        transaction {
            // Crear las tablas necesarias
            SchemaUtils.createMissingTablesAndColumns(
                Users,
                Accounts,
                Transactions,
                Budgets,
                HistoryTracks
            )
        }

        transaction {
            // Limpiar todas las tablas
            Transactions.deleteAll()
            Budgets.deleteAll()
            Accounts.deleteAll()
            Users.deleteAll()
        }

        // Crear usuario de prueba
        transaction {
            Users.insert {
                it[id] = 1
                it[name] = "Test User"
                it[email] = "test@test.com"
                it[password] = "password"
            }
        }

        // Crear cuenta asociada al usuario
        transaction {
            Accounts.insert {
                it[id] = getTestAccountUUID()
                it[name] = "Test Account"
                it[type] = AccountType.CHECKING
                it[initialBalance] = 1000.0.toBigDecimal()
                it[currency] = "COP"
                it[user] = 1
            }
        }
    }

    private fun getTestAccountUUID(): UUID {
        return UUID.fromString("00000000-0000-0000-0000-000000000001")
    }

    fun getTestAccountId(): UUID {
        return transaction {
            Accounts.selectAll().first()[Accounts.id].value
        }
    }



    @OptIn(ExperimentalTime::class)
    @Test
    fun `should return budget progress with spent amount`() {
        val userId = 1
        val accountId = getTestAccountId()
        val timeZone = TimeZone.currentSystemDefault()
        val today = Clock.System.now().toLocalDateTime(timeZone).date

        val startDate = today.minus(DatePeriod(days = 1))
        val endDate = today.plus(DatePeriod(days = 5))

        transaction {
            Budgets.insert {
                it[id] = UUID.randomUUID()
                it[user] = userId
                it[name] = "Test Budget"
                it[category] = "FOOD"
                it[amount] = 500.0.toBigDecimal()
                it[this.startDate] = startDate
                it[this.endDate] = endDate
                it[frequency] = Frequency.WEEKLY
            }
        }

        transactionService.createTransaction(
            userId = userId,
            amount = 100.0,
            description = "Groceries",
            date = today.atTime(12, 0).formatDefault(),
            type = TransactionType.EXPENSE,
            category = "FOOD",
            accountId = accountId
        )

        val results = service.getAllWithProgress(userId, referenceDate = today)
        assertEquals(1, results.size)

        val progress = results[0]
        assertEquals("FOOD", progress.budget.category)
        assertEquals(100.0, progress.spent, 0.001)
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun `should only include transactions in current period for monthly budget`() {
        val userId = 1
        val accountId = getTestAccountId()
        val timeZone = TimeZone.currentSystemDefault()
        val today = Clock.System.now().toLocalDateTime(timeZone).date

        val startDate = LocalDate(today.year, today.month, 1)
        val endDate = startDate.plus(DatePeriod(months = 1)).minus(DatePeriod(days = 1))

        transaction {
            Budgets.insert {
                it[id] = UUID.randomUUID()
                it[user] = userId
                it[name] = "Monthly Budget"
                it[category] = "FOOD"
                it[amount] = 500.0.toBigDecimal()
                it[this.startDate] = startDate
                it[this.endDate] = endDate
                it[frequency] = Frequency.MONTHLY
            }
        }

        transactionService.createTransaction(
            userId = userId,
            amount = 100.0,
            description = "Current month",
            date = today.atTime(10, 0).formatDefault(),
            type = TransactionType.EXPENSE,
            category = "FOOD",
            accountId = accountId
        )

        val lastMonthDate = today.minus(DatePeriod(months = 1)).atTime(10, 0).formatDefault()
        transactionService.createTransaction(
            userId = userId,
            amount = 200.0,
            description = "Last month",
            date = lastMonthDate,
            type = TransactionType.EXPENSE,
            category = "FOOD",
            accountId = accountId
        )

        val result = service.getAllWithProgress(userId, referenceDate = today)
        assertEquals(1, result.size)

        val progress = result[0]
        assertEquals("FOOD", progress.budget.category)
        assertEquals(100.0, progress.spent, 0.001)
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun `should include unbudgeted transactions in response`() {
        val userId = 1
        val accountId = getTestAccountId()
        val timeZone = TimeZone.currentSystemDefault()
        val today = Clock.System.now().toLocalDateTime(timeZone).date

        val startDate = today.minus(DatePeriod(days = 1))
        val endDate = today.plus(DatePeriod(days = 5))

        transaction {
            Budgets.insert {
                it[id] = UUID.randomUUID()
                it[user] = userId
                it[name] = "Food Budget"
                it[category] = "FOOD"
                it[amount] = 500.0.toBigDecimal()
                it[this.startDate] = startDate
                it[this.endDate] = endDate
                it[frequency] = Frequency.WEEKLY
            }
        }

        transactionService.createTransaction(
            userId = userId,
            amount = 100.0,
            description = "Groceries",
            date = today.atTime(12, 0).formatDefault(),
            type = TransactionType.EXPENSE,
            category = "FOOD",
            accountId = accountId
        )

        transactionService.createTransaction(
            userId = userId,
            amount = 50.0,
            description = "Bus ticket",
            date = today.atTime(14, 0).formatDefault(),
            type = TransactionType.EXPENSE,
            category = "TRANSPORTATION",
            accountId = accountId
        )

        val results = service.getAllWithProgress(userId, referenceDate = today)
        assertEquals(2, results.size)

        val unbudgeted = results.find { it.budget.id == "unbudgeted" }
        assertEquals(50.0, unbudgeted?.spent ?: 0.0, 0.001)
        assertEquals("UNBUDGETED", unbudgeted?.budget?.category)
    }
}