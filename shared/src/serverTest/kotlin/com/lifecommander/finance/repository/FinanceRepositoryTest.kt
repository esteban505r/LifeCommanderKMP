package com.lifecommander.finance.repository

import com.lifecommander.finance.model.*
import kotlinx.datetime.*
import kotlin.test.*

class FinanceRepositoryTest {
    private lateinit var repository: FinanceRepository

    @BeforeTest
    fun setup() {
        repository = LocalFinanceRepository()
    }

    @Test
    fun `test addTransaction adds transaction`() {
        // Given
        val transaction = Transaction("1", "Grocery Shopping", 50.0, TransactionType.EXPENSE, Category.FOOD, Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()), "1")

        // When
        repository.addTransaction(transaction)

        // Then
        val transactions = repository.getTransactions()
        assertEquals(1, transactions.size)
        assertEquals(transaction, transactions[0])
    }

    @Test
    fun `test getTransactions returns all transactions`() {
        // Given
        val transaction1 = Transaction("1", "Grocery Shopping", 50.0, TransactionType.EXPENSE, Category.FOOD, Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()), "1")
        val transaction2 = Transaction("2", "Salary", 3000.0, TransactionType.INCOME, Category.SALARY, Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()), "1")
        repository.addTransaction(transaction1)
        repository.addTransaction(transaction2)

        // When
        val transactions = repository.getTransactions()

        // Then
        assertEquals(2, transactions.size)
        assertTrue(transactions.contains(transaction1))
        assertTrue(transactions.contains(transaction2))
    }

    @Test
    fun `test addAccount adds account`() {
        // Given
        val account = Account("1", "Checking", 1000.0)

        // When
        repository.addAccount(account)

        // Then
        val accounts = repository.getAccounts()
        assertEquals(1, accounts.size)
        assertEquals(account, accounts[0])
    }

    @Test
    fun `test getAccounts returns all accounts`() {
        // Given
        val account1 = Account("1", "Checking", 1000.0)
        val account2 = Account("2", "Savings", 5000.0)
        repository.addAccount(account1)
        repository.addAccount(account2)

        // When
        val accounts = repository.getAccounts()

        // Then
        assertEquals(2, accounts.size)
        assertTrue(accounts.contains(account1))
        assertTrue(accounts.contains(account2))
    }

    @Test
    fun `test addBudget adds budget`() {
        // Given
        val budget = Budget("1", "Groceries", Category.FOOD, 500.0, Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()), Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).plus(DatePeriod(months = 1)))

        // When
        repository.addBudget(budget)

        // Then
        val budgets = repository.getBudgets()
        assertEquals(1, budgets.size)
        assertEquals(budget, budgets[0])
    }

    @Test
    fun `test getBudgets returns all budgets`() {
        // Given
        val budget1 = Budget("1", "Groceries", Category.FOOD, 500.0, Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()), Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).plus(DatePeriod(months = 1)))
        val budget2 = Budget("2", "Entertainment", Category.ENTERTAINMENT, 200.0, Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()), Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).plus(DatePeriod(months = 1)))
        repository.addBudget(budget1)
        repository.addBudget(budget2)

        // When
        val budgets = repository.getBudgets()

        // Then
        assertEquals(2, budgets.size)
        assertTrue(budgets.contains(budget1))
        assertTrue(budgets.contains(budget2))
    }

    @Test
    fun `test addSavingsGoal adds goal`() {
        // Given
        val goal = SavingsGoal("1", "New Car", 20000.0, 5000.0, 500.0, Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).plus(DatePeriod(years = 1)))

        // When
        repository.addSavingsGoal(goal)

        // Then
        val goals = repository.getSavingsGoals()
        assertEquals(1, goals.size)
        assertEquals(goal, goals[0])
    }

    @Test
    fun `test getSavingsGoals returns all goals`() {
        // Given
        val goal1 = SavingsGoal("1", "New Car", 20000.0, 5000.0, 500.0, Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).plus(DatePeriod(years = 1)))
        val goal2 = SavingsGoal("2", "Vacation", 5000.0, 1000.0, 200.0, Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).plus(DatePeriod(months = 6)))
        repository.addSavingsGoal(goal1)
        repository.addSavingsGoal(goal2)

        // When
        val goals = repository.getSavingsGoals()

        // Then
        assertEquals(2, goals.size)
        assertTrue(goals.contains(goal1))
        assertTrue(goals.contains(goal2))
    }

    @Test
    fun `test getBudgetProgress calculates correct progress`() {
        // Given
        val budget = Budget("1", "Groceries", Category.FOOD, 500.0, Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()), Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).plus(DatePeriod(months = 1)))
        repository.addBudget(budget)
        val transaction = Transaction("1", "Grocery Shopping", 50.0, TransactionType.EXPENSE, Category.FOOD, Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()), "1")
        repository.addTransaction(transaction)

        // When
        val progress = repository.getBudgetProgress("1")

        // Then
        assertEquals(50.0, progress.spent)
        assertEquals(450.0, progress.remaining)
        assertEquals(10.0, progress.percentageUsed)
        assertFalse(progress.isOverBudget)
    }

    @Test
    fun `test getSavingsGoalProgress calculates correct progress`() {
        // Given
        val goal = SavingsGoal("1", "New Car", 20000.0, 5000.0, 500.0, Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).plus(DatePeriod(years = 1)))
        repository.addSavingsGoal(goal)

        // When
        val progress = repository.getSavingsGoalProgress("1")

        // Then
        assertEquals(25.0, progress.percentageComplete)
        assertEquals(15000.0, progress.remainingAmount)
        assertEquals(500.0, progress.monthlyContributionNeeded)
    }
} 