package com.lifecommander.finance.repository

import com.esteban.ruano.lifecommander.models.finance.Budget
import com.esteban.ruano.lifecommander.models.finance.BudgetProgress
import com.esteban.ruano.lifecommander.models.finance.Category
import com.lifecommander.finance.model.*
import kotlinx.datetime.LocalDateTime

interface FinanceRepository {
    // Transaction operations
    suspend fun addTransaction(transaction: Transaction)
    suspend fun updateTransaction(transaction: Transaction)
    suspend fun deleteTransaction(id: String)
    suspend fun getTransaction(id: String): Transaction?
    suspend fun getTransactions(
        startDate: LocalDateTime? = null,
        endDate: LocalDateTime? = null,
        category: Category? = null,
        accountId: String? = null
    ): List<Transaction>
    
    // Account operations
    suspend fun addAccount(account: Account)
    suspend fun updateAccount(account: Account)
    suspend fun deleteAccount(id: String)
    suspend fun getAccount(id: String): Account?
    suspend fun getAccounts(): List<Account>
    suspend fun calculateAccountBalance(accountId: String): Double
    
    // Budget operations
    suspend fun addBudget(budget: Budget)
    suspend fun updateBudget(budget: Budget)
    suspend fun deleteBudget(id: String)
    suspend fun getBudget(id: String): Budget?
    suspend fun getBudgets(): List<Budget>
    suspend fun calculateBudgetProgress(budgetId: String): BudgetProgress
    suspend fun getOverBudgetCategories(): List<Category>
    
    // Savings goal operations
    suspend fun addSavingsGoal(goal: SavingsGoal)
    suspend fun updateSavingsGoal(goal: SavingsGoal)
    suspend fun deleteSavingsGoal(id: String)
    suspend fun getSavingsGoal(id: String): SavingsGoal?
    suspend fun getSavingsGoals(): List<SavingsGoal>
    suspend fun calculateSavingsGoalProgress(goalId: String): SavingsGoalProgress
} 