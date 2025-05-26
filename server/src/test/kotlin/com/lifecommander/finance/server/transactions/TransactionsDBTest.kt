package com.lifecommander.finance.server.transactions

import com.esteban.ruano.database.entities.*
import com.esteban.ruano.database.models.Status
import com.esteban.ruano.lifecommander.models.finance.TransactionFilters
import com.esteban.ruano.service.TransactionService
import com.esteban.ruano.utils.DateUIUtils.formatDefault
import com.esteban.ruano.utils.DateUtils.formatDateTime
import com.esteban.ruano.utils.DateUtils.toLocalDate
import com.lifecommander.finance.model.AccountType
import com.lifecommander.finance.model.TransactionType
import kotlinx.datetime.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.Before
import org.junit.Test
import java.sql.Connection
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class TransactionsDBTest {
    private lateinit var service: TransactionService

    @Before
    fun setup() {
        service = TransactionService()
        Database.connect(
            url = "jdbc:postgresql://localhost:5431/testdb",
            driver = "org.postgresql.Driver",
            user = "testuser",
            password = "testpassword"
        )
        TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE

        val currentDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val timeZone = TimeZone.currentSystemDefault()
        val yesterdayInstant = Clock.System.now() - 1L.toDuration(DurationUnit.DAYS)
        val yesterdayDateTime = yesterdayInstant.toLocalDateTime(timeZone)


        transaction {
            SchemaUtils.createMissingTablesAndColumns(
                Users,
                Accounts,
                Transactions,
                CategoryKeywords
            )
        }


        transaction {
            Transactions.deleteAll()
            Accounts.deleteAll()
            Users.deleteAll()
        }

        // Create test user
        transaction {
            Users.insert {
                it[id] = 1
                it[name] = "Test User"
                it[email] = "test@test.com"
                it[password] = "password"
            }
        }

        // Create test account
        transaction {
            Accounts.insert {
                it[id] = UUID.randomUUID()
                it[name] = "Test Account"
                it[type] = AccountType.CHECKING
                it[initialBalance] = 1000.0.toBigDecimal()
                it[currency] = "COP"
                it[user] = 1
            }
        }
    }

    @Test
    fun `should create a new transaction`() {
        val accountId = getTestAccountId()
        val transactionId = service.createTransaction(
            userId = 1,
            amount = 100.0,
            description = "Test Transaction",
            date = formatDateTime(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())),
            type = TransactionType.EXPENSE,
            category = "FOOD",
            accountId = accountId
        )

        assertNotNull(transactionId)
        
        val createdTransaction = service.getTransaction(transactionId!!, 1)
        assertNotNull(createdTransaction)
        assertEquals(100.0, createdTransaction!!.amount)
        assertEquals("Test Transaction", createdTransaction.description)
        assertEquals(TransactionType.EXPENSE.toString(), createdTransaction.type.toString())
        assertEquals("FOOD", createdTransaction.category)
    }

    @Test
    fun `should get transactions by user id`() {
        val accountId = getTestAccountId()
        createTestTransaction(accountId)
        
        val transactions = service.getTransactionsByUser(1)
        assertTrue(transactions.transactions.isNotEmpty())
        assertEquals(1, transactions.transactions.size)
    }

    @Test
    fun `should get transactions by account id`() {
        val accountId = getTestAccountId()
        createTestTransaction(accountId)
        
        val transactions = service.getTransactionsByAccount(accountId)
        assertTrue(transactions.isNotEmpty())
        assertEquals(1, transactions.size)
    }

    @Test
    fun `should get transactions by date range`() {
        val accountId = getTestAccountId()
        val currentDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        createTestTransaction(accountId)

        val timeZone = TimeZone.currentSystemDefault()
        val currentInstant = currentDate.toInstant(timeZone)

        val startInstant = currentInstant.minus(1, DateTimeUnit.DAY, timeZone)
        val endInstant = currentInstant.plus(1, DateTimeUnit.DAY, timeZone)

        val startDateTime = startInstant.toLocalDateTime(timeZone)
        val endDateTime = endInstant.toLocalDateTime(timeZone)

        val transactions = service.getTransactionsByUser(
            userId = 1,
            filters = TransactionFilters(
                startDate = startDateTime.toLocalDate().formatDefault(),
                endDate = endDateTime.toLocalDate().formatDefault()
            )
        )
        assertTrue(transactions.transactions.isNotEmpty())
        assertEquals(1, transactions.transactions.size)
    }

    @Test
    fun `should update an existing transaction`() {
        val accountId = getTestAccountId()
        val transactionId = createTestTransaction(accountId)
        
        val wasUpdated = service.updateTransaction(
            transactionId = transactionId,
            userId = 1,
            amount = 200.0,
            description = "Updated Transaction",
            type = TransactionType.INCOME
        )
        
        assertTrue(wasUpdated)
        
        val updatedTransaction = service.getTransaction(transactionId, 1)
        assertNotNull(updatedTransaction)
        assertEquals(200.0, updatedTransaction!!.amount)
        assertEquals("Updated Transaction", updatedTransaction.description)
        assertEquals(TransactionType.INCOME.toString(), updatedTransaction.type.toString())
    }

    @Test
    fun `should delete a transaction`() {
        val accountId = getTestAccountId()
        val transactionId = createTestTransaction(accountId)
        
        val wasDeleted = service.deleteTransaction(transactionId, 1)
        assertTrue(wasDeleted)
        
        val deletedTransaction = service.getTransaction(transactionId, 1)
        assertNull(deletedTransaction)
    }

    @Test
    fun `should import transactions from text`() {
        val accountId = getTestAccountId()
        val text = """
            Compra TECNIPAGOS S A
            
            10 de mayo, 2025
            
            - ${'$'}13.000,00
            
            
            Compra UBER RIDES
            
            10 de mayo, 2025
            
            - ${'$'}8.743,00
        """.trimIndent()
        
        val transactionIds = service.importTransactionsFromText(1, text, accountId.toString())
        assertTrue(transactionIds.isNotEmpty())
        assertEquals(2, transactionIds.size)
    }

    @Test
    fun `should preview transaction import`() {
        val accountId = getTestAccountId()
        val text = """
            Compra TECNIPAGOS S A
            
            10 de mayo, 2025
            
            - ${'$'}13.000,00
            
            
            Compra UBER RIDES
            
            10 de mayo, 2025
            
            - ${'$'}8.743,00
        """.trimIndent()

        try {
            val preview = service.previewTransactionImport(1, text, accountId.toString())

            assertEquals(2, preview.totalTransactions)
            assertEquals(0, preview.duplicateCount)
            assertEquals(21743.0, preview.totalAmount)
        }
        catch (e: Exception) {
            // Handle exception if needed
            println("Error during preview: ${e.message}")
        }
    }

    @Test
    fun `should handle duplicate transactions during import`() {
        val accountId = getTestAccountId()
        val text = """
            Compra TECNIPAGOS S A
            
            10 de mayo, 2025
            
            - ${'$'}13.000,00
            
            
            Compra UBER RIDES
            
            10 de mayo, 2025
            
            - ${'$'}8.743,00
        """.trimIndent()
        
        // First import
        service.importTransactionsFromText(1, text, accountId.toString())
        
        // Second import (should detect duplicates)
        val preview = service.previewTransactionImport(1, text, accountId.toString())
        assertEquals(2, preview.totalTransactions)
        assertEquals(2, preview.duplicateCount)
        
        // Import with skipDuplicates = true
        val transactionIds = service.importTransactionsFromText(1, text, accountId.toString(), skipDuplicates = true)
        assertTrue(transactionIds.isEmpty())
        
        // Import with skipDuplicates = false
        val transactionIds2 = service.importTransactionsFromText(1, text, accountId.toString(), skipDuplicates = false)
        assertEquals(2, transactionIds2.size)
    }

    private fun getTestAccountId(): UUID {
        return transaction {
            Account.find { Accounts.user eq 1 }.first().id.value
        }
    }

    private fun createTestTransaction(accountId: UUID): UUID {
        return service.createTransaction(
            userId = 1,
            amount = 100.0,
            description = "Test Transaction",
            date = formatDateTime(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())),
            type = TransactionType.EXPENSE,
            category = "FOOD",
            accountId = accountId
        )!!
    }
} 