package com.lifecommander.finance.server

import com.lifecommander.finance.model.*
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.*
import kotlinx.serialization.json.Json
import kotlin.test.*

class FinanceServerTest {
    private val repository = LocalFinanceRepository()
    private val server = FinanceServer(repository)
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    @BeforeTest
    fun setup() {
        server.start(8080)
    }

    @AfterTest
    fun tearDown() {
        // Stop the server
        // Note: In a real test, we would need to properly stop the server
        // This is just a placeholder
    }

    @Test
    fun `test getTransactions returns transactions`() = runBlocking {
        // Given
        val transaction = Transaction("1", "Grocery Shopping", 50.0, TransactionType.EXPENSE, Category.FOOD, Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()), "1")
        repository.addTransaction(transaction)

        // When
        val transactions = client.get("http://localhost:8080/api/transactions").body<List<Transaction>>()

        // Then
        assertEquals(1, transactions.size)
        assertEquals(transaction, transactions[0])
    }

    @Test
    fun `test addTransaction adds transaction`() = runBlocking {
        // Given
        val transaction = Transaction("1", "Grocery Shopping", 50.0, TransactionType.EXPENSE, Category.FOOD, Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()), "1")

        // When
        val addedTransaction = client.post("http://localhost:8080/api/transactions") {
            contentType(ContentType.Application.Json)
            setBody(transaction)
        }.body<Transaction>()

        // Then
        assertEquals(transaction, addedTransaction)
        val transactions = repository.getTransactions()
        assertEquals(1, transactions.size)
        assertEquals(transaction, transactions[0])
    }

    @Test
    fun `test getAccounts returns accounts`() = runBlocking {
        // Given
        val account = Account("1", "Checking", 1000.0)
        repository.addAccount(account)

        // When
        val accounts = client.get("http://localhost:8080/api/accounts").body<List<Account>>()

        // Then
        assertEquals(1, accounts.size)
        assertEquals(account, accounts[0])
    }

    @Test
    fun `test addAccount adds account`() = runBlocking {
        // Given
        val account = Account("1", "Checking", 1000.0)

        // When
        val addedAccount = client.post("http://localhost:8080/api/accounts") {
            contentType(ContentType.Application.Json)
            setBody(account)
        }.body<Account>()

        // Then
        assertEquals(account, addedAccount)
        val accounts = repository.getAccounts()
        assertEquals(1, accounts.size)
        assertEquals(account, accounts[0])
    }

    @Test
    fun `test getBudgets returns budgets`() = runBlocking {
        // Given
        val budget = Budget("1", "Groceries", Category.FOOD, 500.0, Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()), Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).plus(DatePeriod(months = 1)))
        repository.addBudget(budget)

        // When
        val budgets = client.get("http://localhost:8080/api/budgets").body<List<Budget>>()

        // Then
        assertEquals(1, budgets.size)
        assertEquals(budget, budgets[0])
    }

    @Test
    fun `test addBudget adds budget`() = runBlocking {
        // Given
        val budget = Budget("1", "Groceries", Category.FOOD, 500.0, Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()), Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).plus(DatePeriod(months = 1)))

        // When
        val addedBudget = client.post("http://localhost:8080/api/budgets") {
            contentType(ContentType.Application.Json)
            setBody(budget)
        }.body<Budget>()

        // Then
        assertEquals(budget, addedBudget)
        val budgets = repository.getBudgets()
        assertEquals(1, budgets.size)
        assertEquals(budget, budgets[0])
    }

    @Test
    fun `test getSavingsGoals returns goals`() = runBlocking {
        // Given
        val goal = SavingsGoal("1", "New Car", 20000.0, 5000.0, 500.0, Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).plus(DatePeriod(years = 1)))
        repository.addSavingsGoal(goal)

        // When
        val goals = client.get("http://localhost:8080/api/savings-goals").body<List<SavingsGoal>>()

        // Then
        assertEquals(1, goals.size)
        assertEquals(goal, goals[0])
    }

    @Test
    fun `test addSavingsGoal adds goal`() = runBlocking {
        // Given
        val goal = SavingsGoal("1", "New Car", 20000.0, 5000.0, 500.0, Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).plus(DatePeriod(years = 1)))

        // When
        val addedGoal = client.post("http://localhost:8080/api/savings-goals") {
            contentType(ContentType.Application.Json)
            setBody(goal)
        }.body<SavingsGoal>()

        // Then
        assertEquals(goal, addedGoal)
        val goals = repository.getSavingsGoals()
        assertEquals(1, goals.size)
        assertEquals(goal, goals[0])
    }

    @Test
    fun `test getBudgetProgress returns progress`() = runBlocking {
        // Given
        val budget = Budget("1", "Groceries", Category.FOOD, 500.0, Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()), Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).plus(DatePeriod(months = 1)))
        repository.addBudget(budget)
        val transaction = Transaction("1", "Grocery Shopping", 50.0, TransactionType.EXPENSE, Category.FOOD, Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()), "1")
        repository.addTransaction(transaction)

        // When
        val progress = client.get("http://localhost:8080/api/budgets/1/progress").body<BudgetProgress>()

        // Then
        assertEquals(50.0, progress.spent)
        assertEquals(450.0, progress.remaining)
        assertEquals(10.0, progress.percentageUsed)
        assertFalse(progress.isOverBudget)
    }

    @Test
    fun `test getSavingsGoalProgress returns progress`() = runBlocking {
        // Given
        val goal = SavingsGoal("1", "New Car", 20000.0, 5000.0, 500.0, Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).plus(DatePeriod(years = 1)))
        repository.addSavingsGoal(goal)

        // When
        val progress = client.get("http://localhost:8080/api/savings-goals/1/progress").body<SavingsGoalProgress>()

        // Then
        assertEquals(25.0, progress.percentageComplete)
        assertEquals(15000.0, progress.remainingAmount)
        assertEquals(500.0, progress.monthlyContributionNeeded)
    }
} 