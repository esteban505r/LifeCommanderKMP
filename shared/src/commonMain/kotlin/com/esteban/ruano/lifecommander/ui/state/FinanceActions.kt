package com.lifecommander.finance.model

import kotlinx.datetime.LocalDateTime

interface FinanceActions {
    // Account actions
    suspend fun addAccount(account: Account)
    suspend fun updateAccount(account: Account)
    suspend fun deleteAccount(id: String)
    suspend fun selectAccount(account: Account?)
    suspend fun getAccounts(): List<Account>
    
    // Transaction actions
    suspend fun addTransaction(transaction: Transaction)
    suspend fun updateTransaction(transaction: Transaction)
    suspend fun deleteTransaction(id: String)
    suspend fun getTransactions(
        startDate: LocalDateTime? = null,
        endDate: LocalDateTime? = null,
        category: Category? = null,
        accountId: String? = null
    ): List<Transaction>
    
    // Budget actions
    suspend fun addBudget(budget: Budget)
    suspend fun updateBudget(budget: Budget)
    suspend fun deleteBudget(id: String)
    suspend fun getBudgets(): List<Budget>
    suspend fun getBudgetProgress(budgetId: String): BudgetProgress?
    
    // Savings goal actions
    suspend fun addSavingsGoal(goal: SavingsGoal)
    suspend fun updateSavingsGoal(goal: SavingsGoal)
    suspend fun deleteSavingsGoal(id: String)
    suspend fun getSavingsGoals(): List<SavingsGoal>
    suspend fun getSavingsGoalProgress(goalId: String): SavingsGoalProgress?
    
    // Data loading
    suspend fun loadData()
} 