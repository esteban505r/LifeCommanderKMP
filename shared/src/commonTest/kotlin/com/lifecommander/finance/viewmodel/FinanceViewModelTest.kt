package com.lifecommander.finance.viewmodel

import com.esteban.ruano.lifecommander.models.finance.Budget
import com.esteban.ruano.lifecommander.models.finance.BudgetProgress
import com.lifecommander.finance.service.FinanceService
import com.lifecommander.finance.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlinx.datetime.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class FinanceViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: FinanceViewModel
    private lateinit var client: FinanceService

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        client = FinanceService("http://localhost:8080")
        viewModel = FinanceViewModel()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test loadData loads all data`() = runTest {
        // Given
        val accounts = listOf(
            Account("1", "Checking", 1000.0),
            Account("2", "Savings", 5000.0)
        )
        val budgets = listOf(
            Budget(
                "1",
                "Groceries",
                Category.FOOD,
                500.0,
                Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).plus(DatePeriod(months = 1))
            )
        )
        val savingsGoals = listOf(
            SavingsGoal("1", "New Car", 20000.0, 5000.0, 500.0, Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).plus(DatePeriod(years = 1)))
        )
        val transactions = listOf(
            Transaction("1", "Grocery Shopping", 50.0, TransactionType.EXPENSE, Category.FOOD, Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()), "1")
        )

        // When
        viewModel.loadData()

        // Then
        assertEquals(accounts, viewModel.accounts.value)
        assertEquals(budgets, viewModel.budgets.value)
        assertEquals(savingsGoals, viewModel.savingsGoals.value)
        assertEquals(transactions, viewModel.transactions.value)
    }

    @Test
    fun `test selectAccount updates selected account and loads transactions`() = runTest {
        // Given
        val account = Account("1", "Checking", 1000.0)
        val transactions = listOf(
            Transaction("1", "Grocery Shopping", 50.0, TransactionType.EXPENSE, Category.FOOD, Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()), "1")
        )

        // When
        viewModel.selectAccount(account)

        // Then
        assertEquals(account, viewModel.selectedAccount.value)
        assertEquals(transactions, viewModel.transactions.value)
    }

    @Test
    fun `test addTransaction adds transaction and updates budget progress`() = runTest {
        // Given
        val transaction = Transaction("1", "Grocery Shopping", 50.0, TransactionType.EXPENSE, Category.FOOD, Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()), "1")
        val budget = Budget(
            "1",
            "Groceries",
            Category.FOOD,
            500.0,
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).plus(DatePeriod(months = 1))
        )
        val budgetProgress = BudgetProgress(budget, 50.0, 450.0, 10.0, false)

        // When
        viewModel.addTransaction(transaction)

        // Then
        assertTrue(viewModel.transactions.value.contains(transaction))
        assertEquals(budgetProgress, viewModel.getBudgetProgress("1"))
    }

    @Test
    fun `test addAccount adds account`() = runTest {
        // Given
        val account = Account("1", "Checking", 1000.0)

        // When
        viewModel.addAccount(account)

        // Then
        assertTrue(viewModel.accounts.value.contains(account))
    }

    @Test
    fun `test addBudget adds budget and loads progress`() = runTest {
        // Given
        val budget = Budget(
            "1",
            "Groceries",
            Category.FOOD,
            500.0,
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).plus(DatePeriod(months = 1))
        )
        val budgetProgress = BudgetProgress(budget, 0.0, 500.0, 0.0, false)

        // When
        viewModel.addBudget(budget)

        // Then
        assertTrue(viewModel.budgets.value.contains(budget))
        assertEquals(budgetProgress, viewModel.getBudgetProgress("1"))
    }

    @Test
    fun `test addSavingsGoal adds goal and loads progress`() = runTest {
        // Given
        val goal = SavingsGoal("1", "New Car", 20000.0, 5000.0, 500.0, Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).plus(DatePeriod(years = 1)))
        val goalProgress = SavingsGoalProgress(goal, 25.0, 15000.0, Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).plus(DatePeriod(years = 1)), 500.0)

        // When
        viewModel.addSavingsGoal(goal)

        // Then
        assertTrue(viewModel.savingsGoals.value.contains(goal))
        assertEquals(goalProgress, viewModel.getSavingsGoalProgress("1"))
    }
} 