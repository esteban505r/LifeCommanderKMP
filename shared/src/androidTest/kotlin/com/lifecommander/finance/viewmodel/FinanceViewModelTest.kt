package com.lifecommander.finance.viewmodel

import com.esteban.ruano.lifecommander.models.finance.Budget
import com.esteban.ruano.lifecommander.models.finance.BudgetProgress
import com.lifecommander.finance.model.*
import com.lifecommander.finance.repository.FinanceRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlinx.datetime.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class FinanceViewModelTest {
    private lateinit var repository: FinanceRepository
    private lateinit var viewModel: FinanceViewModel
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        viewModel = FinanceViewModel(repository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test loadTransactions loads transactions`() = runTest {
        // Given
        val transactions = listOf(
            Transaction("1", "Grocery Shopping", 50.0, TransactionType.EXPENSE, Category.FOOD, Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()), "1"),
            Transaction("2", "Salary", 3000.0, TransactionType.INCOME, Category.SALARY, Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()), "1")
        )
        coEvery { repository.getTransactions() } returns transactions

        // When
        viewModel.loadTransactions()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(transactions, viewModel.transactions.value)
    }

    @Test
    fun `test addTransaction adds transaction`() = runTest {
        // Given
        val transaction = Transaction("1", "Grocery Shopping", 50.0, TransactionType.EXPENSE, Category.FOOD, Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()), "1")
        coEvery { repository.addTransaction(any()) } just Runs
        coEvery { repository.getTransactions() } returns listOf(transaction)

        // When
        viewModel.addTransaction(transaction)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { repository.addTransaction(transaction) }
        assertEquals(listOf(transaction), viewModel.transactions.value)
    }

    @Test
    fun `test loadAccounts loads accounts`() = runTest {
        // Given
        val accounts = listOf(
            Account("1", "Checking", 1000.0),
            Account("2", "Savings", 5000.0)
        )
        coEvery { repository.getAccounts() } returns accounts

        // When
        viewModel.loadAccounts()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(accounts, viewModel.accounts.value)
    }

    @Test
    fun `test addAccount adds account`() = runTest {
        // Given
        val account = Account("1", "Checking", 1000.0)
        coEvery { repository.addAccount(any()) } just Runs
        coEvery { repository.getAccounts() } returns listOf(account)

        // When
        viewModel.addAccount(account)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { repository.addAccount(account) }
        assertEquals(listOf(account), viewModel.accounts.value)
    }

    @Test
    fun `test loadBudgets loads budgets`() = runTest {
        // Given
        val budgets = listOf(
            Budget(
                "1",
                "Groceries",
                Category.FOOD,
                500.0,
                Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).plus(DatePeriod(months = 1))
            ),
            Budget(
                "2",
                "Entertainment",
                Category.ENTERTAINMENT,
                200.0,
                Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).plus(DatePeriod(months = 1))
            )
        )
        coEvery { repository.getBudgets() } returns budgets

        // When
        viewModel.loadBudgets()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(budgets, viewModel.budgets.value)
    }

    @Test
    fun `test addBudget adds budget`() = runTest {
        // Given
        val budget = Budget(
            "1",
            "Groceries",
            Category.FOOD,
            500.0,
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).plus(DatePeriod(months = 1))
        )
        coEvery { repository.addBudget(any()) } just Runs
        coEvery { repository.getBudgets() } returns listOf(budget)

        // When
        viewModel.addBudget(budget)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { repository.addBudget(budget) }
        assertEquals(listOf(budget), viewModel.budgets.value)
    }

    @Test
    fun `test loadSavingsGoals loads goals`() = runTest {
        // Given
        val goals = listOf(
            SavingsGoal("1", "New Car", 20000.0, 5000.0, 500.0, Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).plus(DatePeriod(years = 1))),
            SavingsGoal("2", "Vacation", 5000.0, 1000.0, 200.0, Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).plus(DatePeriod(months = 6)))
        )
        coEvery { repository.getSavingsGoals() } returns goals

        // When
        viewModel.loadSavingsGoals()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(goals, viewModel.savingsGoals.value)
    }

    @Test
    fun `test addSavingsGoal adds goal`() = runTest {
        // Given
        val goal = SavingsGoal("1", "New Car", 20000.0, 5000.0, 500.0, Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).plus(DatePeriod(years = 1)))
        coEvery { repository.addSavingsGoal(any()) } just Runs
        coEvery { repository.getSavingsGoals() } returns listOf(goal)

        // When
        viewModel.addSavingsGoal(goal)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { repository.addSavingsGoal(goal) }
        assertEquals(listOf(goal), viewModel.savingsGoals.value)
    }

    @Test
    fun `test getBudgetProgress gets progress`() = runTest {
        // Given
        val progress = BudgetProgress(50.0, 450.0, 10.0, false)
        coEvery { repository.getBudgetProgress(any()) } returns progress

        // When
        val result = viewModel.getBudgetProgress("1")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(progress, result)
    }

    @Test
    fun `test getSavingsGoalProgress gets progress`() = runTest {
        // Given
        val progress = SavingsGoalProgress(25.0, 15000.0, 500.0)
        coEvery { repository.getSavingsGoalProgress(any()) } returns progress

        // When
        val result = viewModel.getSavingsGoalProgress("1")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(progress, result)
    }
} 