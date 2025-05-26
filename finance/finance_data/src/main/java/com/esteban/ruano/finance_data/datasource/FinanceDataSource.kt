package com.esteban.ruano.finance_data.datasource

import com.esteban.ruano.lifecommander.models.finance.*
import com.lifecommander.finance.model.*

interface FinanceDataSource {
    // Transaction operations
    suspend fun getTransactions(
        filter: String? = null,
        page: Int? = null,
        limit: Int? = null,
        filters: TransactionFilters = TransactionFilters()
    ): TransactionsResponse

    suspend fun getTransaction(transactionId: String): Transaction
    suspend fun addTransaction(transaction: Transaction): Transaction
    suspend fun updateTransaction(transaction: Transaction): Transaction
    suspend fun deleteTransaction(id: String)

    // Account operations
    suspend fun getAccounts(): List<Account>
    suspend fun addAccount(account: Account): Account
    suspend fun updateAccount(account: Account): Account
    suspend fun deleteAccount(id: String)

    // Budget operations
    suspend fun getBudgets(
        filters: BudgetFilters = BudgetFilters(),
        referenceDate: String
    ): List<BudgetProgress>
    suspend fun addBudget(budget: Budget): Budget
    suspend fun updateBudget(budget: Budget): Budget
    suspend fun deleteBudget(id: String)
    suspend fun getBudgetProgress(budgetId: String): BudgetProgress
    suspend fun getBudgetTransactions(
        budgetId: String,
        referenceDate: String,
        filters: TransactionFilters = TransactionFilters()
    ): List<Transaction>

    // Savings Goal operations
    suspend fun getSavingsGoals(): List<SavingsGoal>
    suspend fun addSavingsGoal(goal: SavingsGoal): SavingsGoal
    suspend fun updateSavingsGoal(goal: SavingsGoal): SavingsGoal
    suspend fun deleteSavingsGoal(id: String)
    suspend fun getSavingsGoalProgress(goalId: String): SavingsGoalProgress

    // Scheduled Transaction operations
    suspend fun getScheduledTransactions(
        page: Int? = null,
        limit: Int? = null,
        filters: TransactionFilters = TransactionFilters()
    ): ScheduledTransactionsResponse
    suspend fun addScheduledTransaction(transaction: ScheduledTransaction): ScheduledTransaction
    suspend fun updateScheduledTransaction(transaction: ScheduledTransaction): ScheduledTransaction
    suspend fun deleteScheduledTransaction(id: String)

    // Utility operations
    suspend fun categorizeAllTransactions(referenceDate: String)
    suspend fun categorizeUnbudgeted(referenceDate: String)
    suspend fun importTransactions(text: String, accountId: String, skipDuplicates: Boolean): List<String>
    suspend fun previewTransactionImport(text: String, accountId: String): TransactionImportPreview
} 