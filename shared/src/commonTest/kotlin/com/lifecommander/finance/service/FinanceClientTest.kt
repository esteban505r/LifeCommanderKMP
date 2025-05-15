package com.lifecommander.finance.service

import com.lifecommander.finance.model.*
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.*
import kotlinx.serialization.json.Json
import kotlin.test.*

class FinanceClientTest {
    private val server = embeddedServer(Netty, port = 8080) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }

        routing {
            // Mock endpoints
            route("/api/transactions") {
                get {
                    val startDate = call.request.queryParameters["startDate"]?.let { LocalDateTime.parse(it) }
                    val endDate = call.request.queryParameters["endDate"]?.let { LocalDateTime.parse(it) }
                    val category = call.request.queryParameters["category"]?.let { Category.valueOf(it) }
                    val accountId = call.request.queryParameters["accountId"]

                    val transactions = listOf(
                        Transaction("1", "Grocery Shopping", 50.0, TransactionType.EXPENSE, Category.FOOD, Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()), "1")
                    )

                    call.respond(transactions)
                }

                post {
                    val transaction = call.receive<Transaction>()
                    call.respond(transaction)
                }
            }

            route("/api/accounts") {
                get {
                    val accounts = listOf(
                        Account("1", "Checking", 1000.0),
                        Account("2", "Savings", 5000.0)
                    )
                    call.respond(accounts)
                }

                post {
                    val account = call.receive<Account>()
                    call.respond(account)
                }
            }

            route("/api/budgets") {
                get {
                    val budgets = listOf(
                        Budget("1", "Groceries", Category.FOOD, 500.0, Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()), Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).plus(DatePeriod(months = 1)))
                    )
                    call.respond(budgets)
                }

                post {
                    val budget = call.receive<Budget>()
                    call.respond(budget)
                }
            }

            route("/api/savings-goals") {
                get {
                    val goals = listOf(
                        SavingsGoal("1", "New Car", 20000.0, 5000.0, 500.0, Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).plus(DatePeriod(years = 1)))
                    )
                    call.respond(goals)
                }

                post {
                    val goal = call.receive<SavingsGoal>()
                    call.respond(goal)
                }
            }
        }
    }

    private val client = FinanceService("http://localhost:8080")

    @BeforeTest
    fun setup() {
        server.start()
    }

    @AfterTest
    fun tearDown() {
        server.stop(1000, 2000)
    }

    @Test
    fun `test getTransactions returns transactions`() = runBlocking {
        // When
        val transactions = client.getTransactions()

        // Then
        assertEquals(1, transactions.size)
        assertEquals("Grocery Shopping", transactions[0].description)
        assertEquals(50.0, transactions[0].amount)
        assertEquals(TransactionType.EXPENSE, transactions[0].type)
        assertEquals(Category.FOOD, transactions[0].category)
    }

    @Test
    fun `test getAccounts returns accounts`() = runBlocking {
        // When
        val accounts = client.getAccounts()

        // Then
        assertEquals(2, accounts.size)
        assertEquals("Checking", accounts[0].name)
        assertEquals(1000.0, accounts[0].balance)
        assertEquals("Savings", accounts[1].name)
        assertEquals(5000.0, accounts[1].balance)
    }

    @Test
    fun `test getBudgets returns budgets`() = runBlocking {
        // When
        val budgets = client.getBudgets()

        // Then
        assertEquals(1, budgets.size)
        assertEquals("Groceries", budgets[0].name)
        assertEquals(Category.FOOD, budgets[0].category)
        assertEquals(500.0, budgets[0].amount)
    }

    @Test
    fun `test getSavingsGoals returns goals`() = runBlocking {
        // When
        val goals = client.getSavingsGoals()

        // Then
        assertEquals(1, goals.size)
        assertEquals("New Car", goals[0].name)
        assertEquals(20000.0, goals[0].targetAmount)
        assertEquals(5000.0, goals[0].currentAmount)
        assertEquals(500.0, goals[0].monthlyContribution)
    }

    @Test
    fun `test addTransaction adds transaction`() = runBlocking {
        // Given
        val transaction = Transaction("1", "Grocery Shopping", 50.0, TransactionType.EXPENSE, Category.FOOD, Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()), "1")

        // When
        val addedTransaction = client.addTransaction(transaction)

        // Then
        assertEquals(transaction, addedTransaction)
    }

    @Test
    fun `test addAccount adds account`() = runBlocking {
        // Given
        val account = Account("1", "Checking", 1000.0)

        // When
        val addedAccount = client.addAccount(account)

        // Then
        assertEquals(account, addedAccount)
    }

    @Test
    fun `test addBudget adds budget`() = runBlocking {
        // Given
        val budget = Budget("1", "Groceries", Category.FOOD, 500.0, Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()), Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).plus(DatePeriod(months = 1)))

        // When
        val addedBudget = client.addBudget(budget)

        // Then
        assertEquals(budget, addedBudget)
    }

    @Test
    fun `test addSavingsGoal adds goal`() = runBlocking {
        // Given
        val goal = SavingsGoal("1", "New Car", 20000.0, 5000.0, 500.0, Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).plus(DatePeriod(years = 1)))

        // When
        val addedGoal = client.addSavingsGoal(goal)

        // Then
        assertEquals(goal, addedGoal)
    }
} 