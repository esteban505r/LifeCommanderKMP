package com.lifecommander.finance.repository

import com.esteban.ruano.lifecommander.models.finance.Budget
import com.lifecommander.finance.model.*
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.*
import kotlin.test.*

class LocalFinanceRepositoryTest {
    private val repository = LocalFinanceRepository()

    @Test
    fun `test addTransaction adds transaction`() = runBlocking {
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
    fun `test updateTransaction updates transaction`() = runBlocking {
        // Given
        val transaction = Transaction("1", "Grocery Shopping", 50.0, TransactionType.EXPENSE, Category.FOOD, Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()), "1")
        repository.addTransaction(transaction)
        val updatedTransaction = transaction.copy(amount = 75.0)

        // When
        repository.updateTransaction(updatedTransaction)

        // Then
        val transactions = repository.getTransactions()
        assertEquals(1, transactions.size)
        assertEquals(updatedTransaction, transactions[0])
    }

    @Test
    fun `test deleteTransaction deletes transaction`() = runBlocking {
        // Given
        val transaction = Transaction("1", "Grocery Shopping", 50.0, TransactionType.EXPENSE, Category.FOOD, Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()), "1")
        repository.addTransaction(transaction)

        // When
        repository.deleteTransaction(transaction.id)

        // Then
        val transactions = repository.getTransactions()
        assertTrue(transactions.isEmpty())
    }

    @Test
    fun `test addAccount adds account`() = runBlocking {
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
    fun `test updateAccount updates account`() = runBlocking {
        // Given
        val account = Account("1", "Checking", 1000.0)
        repository.addAccount(account)
        val updatedAccount = account.copy(balance = 1500.0)

        // When
        repository.updateAccount(updatedAccount)

        // Then
        val accounts = repository.getAccounts()
        assertEquals(1, accounts.size)
        assertEquals(updatedAccount, accounts[0])
    }

    @Test
    fun `test deleteAccount deletes account`() = runBlocking {
        // Given
        val account = Account("1", "Checking", 1000.0)
        repository.addAccount(account)

        // When
        repository.deleteAccount(account.id)

        // Then
        val accounts = repository.getAccounts()
        assertTrue(accounts.isEmpty())
    }

    @Test
    fun `test addBudget adds budget`() = runBlocking {
        // Given
        val budget = Budget(
            "1",
            "Groceries",
            Category.FOOD,
            500.0,
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).plus(DatePeriod(months = 1))
        )

        // When
        repository.addBudget(budget)

        // Then
        val budgets = repository.getBudgets()
        assertEquals(1, budgets.size)
        assertEquals(budget, budgets[0])
    }

    @Test
    fun `test updateBudget updates budget`() = runBlocking {
        // Given
        val budget = Budget(
            "1",
            "Groceries",
            Category.FOOD,
            500.0,
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).plus(DatePeriod(months = 1))
        )
        repository.addBudget(budget)
        val updatedBudget = budget.copy(amount = 750.0)

        // When
        repository.updateBudget(updatedBudget)

        // Then
        val budgets = repository.getBudgets()
        assertEquals(1, budgets.size)
        assertEquals(updatedBudget, budgets[0])
    }

    @Test
    fun `test deleteBudget deletes budget`() = runBlocking {
        // Given
        val budget = Budget(
            "1",
            "Groceries",
            Category.FOOD,
            500.0,
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).plus(DatePeriod(months = 1))
        )
        repository.addBudget(budget)

        // When
        repository.deleteBudget(budget.id)

        // Then
        val budgets = repository.getBudgets()
        assertTrue(budgets.isEmpty())
    }

    @Test
    fun `test addSavingsGoal adds goal`() = runBlocking {
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
    fun `test updateSavingsGoal updates goal`() = runBlocking {
        // Given
        val goal = SavingsGoal("1", "New Car", 20000.0, 5000.0, 500.0, Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).plus(DatePeriod(years = 1)))
        repository.addSavingsGoal(goal)
        val updatedGoal = goal.copy(currentAmount = 7500.0)

        // When
        repository.updateSavingsGoal(updatedGoal)

        // Then
        val goals = repository.getSavingsGoals()
        assertEquals(1, goals.size)
        assertEquals(updatedGoal, goals[0])
    }

    @Test
    fun `test deleteSavingsGoal deletes goal`() = runBlocking {
        // Given
        val goal = SavingsGoal("1", "New Car", 20000.0, 5000.0, 500.0, Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).plus(DatePeriod(years = 1)))
        repository.addSavingsGoal(goal)

        // When
        repository.deleteSavingsGoal(goal.id)

        // Then
        val goals = repository.getSavingsGoals()
        assertTrue(goals.isEmpty())
    }

    @Test
    fun `test calculateAccountBalance calculates correct balance`() = runBlocking {
        // Given
        val account = Account("1", "Checking", 1000.0)
        repository.addAccount(account)
        val transaction1 = Transaction("1", "Grocery Shopping", 50.0, TransactionType.EXPENSE, Category.FOOD, Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()), "1")
        val transaction2 = Transaction("2", "Salary", 2000.0, TransactionType.INCOME, Category.INCOME, Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()), "1")
        repository.addTransaction(transaction1)
        repository.addTransaction(transaction2)

        // When
        val balance = repository.calculateAccountBalance(account.id)

        // Then
        assertEquals(2950.0, balance)
    }

    @Test
    fun `test calculateBudgetProgress calculates correct progress`() = runBlocking {
        // Given
        val budget = Budget(
            "1",
            "Groceries",
            Category.FOOD,
            500.0,
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).plus(DatePeriod(months = 1))
        )
        repository.addBudget(budget)
        val transaction = Transaction("1", "Grocery Shopping", 50.0, TransactionType.EXPENSE, Category.FOOD, Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()), "1")
        repository.addTransaction(transaction)

        // When
        val progress = repository.calculateBudgetProgress(budget.id)

        // Then
        assertEquals(50.0, progress.spent)
        assertEquals(450.0, progress.remaining)
        assertEquals(10.0, progress.percentageUsed)
        assertFalse(progress.isOverBudget)
    }

    @Test
    fun `test calculateSavingsGoalProgress calculates correct progress`() = runBlocking {
        // Given
        val goal = SavingsGoal("1", "New Car", 20000.0, 5000.0, 500.0, Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).plus(DatePeriod(years = 1)))
        repository.addSavingsGoal(goal)

        // When
        val progress = repository.calculateSavingsGoalProgress(goal.id)

        // Then
        assertEquals(25.0, progress.percentageComplete)
        assertEquals(15000.0, progress.remainingAmount)
        assertEquals(500.0, progress.monthlyContributionNeeded)
    }
} 