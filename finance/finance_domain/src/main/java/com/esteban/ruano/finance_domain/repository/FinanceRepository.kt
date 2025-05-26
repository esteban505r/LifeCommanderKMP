package com.esteban.ruano.finance_domain.repository

import com.lifecommander.finance.model.*
import com.esteban.ruano.lifecommander.models.finance.*

interface FinanceRepository {
    // Transaction operations
    suspend fun getTransactions(
        filter: String?,
        page: Int?,
        limit: Int?,
        filters: TransactionFilters
    ): Result<TransactionsResponse>

    suspend fun getTransaction(transactionId: String): Result<Transaction>
    
    suspend fun addTransaction(transaction: Transaction): Result<Transaction>
    
    suspend fun updateTransaction(transaction: Transaction): Result<Transaction>
    
    suspend fun deleteTransaction(id: String): Result<Unit>

    // Account operations
    suspend fun getAccounts(): Result<List<Account>>
    
    suspend fun addAccount(account: Account): Result<Account>
    
    suspend fun updateAccount(account: Account): Result<Account>
    
    suspend fun deleteAccount(id: String): Result<Unit>

    // Budget operations
    suspend fun getBudgets(
        filters: BudgetFilters,
        referenceDate: String
    ): Result<List<BudgetProgress>>
    
    suspend fun addBudget(budget: Budget): Result<Budget>
    
    suspend fun updateBudget(budget: Budget): Result<Budget>
    
    suspend fun deleteBudget(id: String): Result<Unit>
    
    suspend fun getBudgetProgress(budgetId: String): Result<BudgetProgress>
    
    suspend fun getBudgetTransactions(
        budgetId: String,
        referenceDate: String,
        filters: TransactionFilters
    ): Result<List<Transaction>>

    // Savings Goal operations
    suspend fun getSavingsGoals(): Result<List<SavingsGoal>>
    
    suspend fun addSavingsGoal(goal: SavingsGoal): Result<SavingsGoal>
    
    suspend fun updateSavingsGoal(goal: SavingsGoal): Result<SavingsGoal>
    
    suspend fun deleteSavingsGoal(id: String): Result<Unit>
    
    suspend fun getSavingsGoalProgress(goalId: String): Result<SavingsGoalProgress>

    // Scheduled Transaction operations
    suspend fun getScheduledTransactions(
        filter: String?,
        page: Int?,
        limit: Int?,
        filters: TransactionFilters
    ): Result<ScheduledTransactionsResponse>
    
    suspend fun addScheduledTransaction(transaction: ScheduledTransaction): Result<ScheduledTransaction>
    
    suspend fun updateScheduledTransaction(transaction: ScheduledTransaction): Result<ScheduledTransaction>
    
    suspend fun deleteScheduledTransaction(id: String): Result<Unit>

    // Utility operations
    suspend fun categorizeAllTransactions(referenceDate: String): Result<Unit>
    
    suspend fun categorizeUnbudgeted(referenceDate: String): Result<Unit>
    
    suspend fun importTransactions(text: String, accountId: String, skipDuplicates: Boolean): Result<List<String>>
    
    suspend fun previewTransactionImport(text: String, accountId: String): Result<TransactionImportPreview>
} 