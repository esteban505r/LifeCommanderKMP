package com.esteban.ruano.finance_data.datasource

import com.esteban.ruano.finance_data.remote.FinanceApi
import com.esteban.ruano.lifecommander.models.finance.*
import com.lifecommander.finance.model.*

private fun TransactionFilters.toQueryMap(): Map<String, Any?> {
    val map = mutableMapOf<String, Any?>()
    searchPattern?.let { map["searchPattern"] = it }
    categories?.let { map["categories"] = it }
    startDate?.let { map["startDate"] = it }
    startDateHour?.let { map["startDateHour"] = it }
    endDate?.let { map["endDate"] = it }
    endDateHour?.let { map["endDateHour"] = it }
    types?.let { map["types"] = it.map { t -> t.name } }
    minAmount?.let { map["minAmount"] = it }
    maxAmount?.let { map["maxAmount"] = it }
    accountIds?.let { map["accountIds"] = it }
    amountSortOrder.takeIf { it != SortOrder.NONE }?.let { map["amountSortOrder"] = it.name }
    return map
}

private fun BudgetFilters.toQueryMap(): Map<String, Any?> {
    val map = mutableMapOf<String, Any?>()
    searchPattern?.let { map["searchPattern"] = it }
    categories?.let { map["categories"] = it }
    minAmount?.let { map["minAmount"] = it }
    maxAmount?.let { map["maxAmount"] = it }
    isOverBudget?.let { map["isOverBudget"] = it }
    return map
}

class FinanceRemoteDataSource(
    private val api: FinanceApi
) : FinanceDataSource {
    // Transaction operations
    override suspend fun getTransactions(
        filter: String?,
        page: Int?,
        limit: Int?,
        filters: TransactionFilters
    ): TransactionsResponse = api.getTransactions(limit, page, filters.toQueryMap())

    override suspend fun getTransaction(transactionId: String): Transaction = api.getTransaction(transactionId)
    override suspend fun addTransaction(transaction: Transaction): Transaction = api.addTransaction(transaction)
    override suspend fun updateTransaction(transaction: Transaction): Transaction {
        val transactionId = transaction.id ?: throw IllegalArgumentException("Transaction ID is required for update")
        return api.updateTransaction(transactionId, transaction)
    }
    override suspend fun deleteTransaction(id: String) = api.deleteTransaction(id)

    // Account operations
    override suspend fun getAccounts(): List<Account> = api.getAccounts()
    override suspend fun addAccount(account: Account): Account = api.addAccount(account)
    override suspend fun updateAccount(account: Account): Account {
        val accountId = account.id ?: throw IllegalArgumentException("Account ID is required for update")
        return api.updateAccount(accountId, account)
    }
    override suspend fun deleteAccount(id: String) = api.deleteAccount(id)

    // Budget operations
    override suspend fun getBudgets(
        filters: BudgetFilters,
        referenceDate: String
    ): List<BudgetProgress> = api.getBudgetsWithProgress(filters.toQueryMap(), referenceDate)
    override suspend fun addBudget(budget: Budget): Budget = api.addBudget(budget)
    override suspend fun updateBudget(budget: Budget): Budget {
        val budgetId = budget.id ?: throw IllegalArgumentException("Budget ID is required for update")
        return api.updateBudget(budgetId, budget)
    }
    override suspend fun deleteBudget(id: String) = api.deleteBudget(id)
    override suspend fun getBudgetProgress(budgetId: String): BudgetProgress = api.getBudgetProgress(budgetId)
    override suspend fun getBudgetTransactions(
        budgetId: String,
        referenceDate: String,
        filters: TransactionFilters
    ): List<Transaction> = api.getBudgetTransactions(budgetId, referenceDate, filters.toQueryMap())

    // Savings Goal operations
    override suspend fun getSavingsGoals(): List<SavingsGoal> = api.getSavingsGoals()
    override suspend fun addSavingsGoal(goal: SavingsGoal): SavingsGoal = api.addSavingsGoal(goal)
    override suspend fun updateSavingsGoal(goal: SavingsGoal): SavingsGoal {
        val goalId = goal.id ?: throw IllegalArgumentException("Savings goal ID is required for update")
        return api.updateSavingsGoal(goalId, goal)
    }
    override suspend fun deleteSavingsGoal(id: String) = api.deleteSavingsGoal(id)
    override suspend fun getSavingsGoalProgress(goalId: String): SavingsGoalProgress = api.getSavingsGoalProgress(goalId)

    // Scheduled Transaction operations
    override suspend fun getScheduledTransactions(
        page: Int?,
        limit: Int?,
        filters: TransactionFilters
    ): ScheduledTransactionsResponse = api.getScheduledTransactions(limit, page, filters.toQueryMap())
    override suspend fun addScheduledTransaction(transaction: ScheduledTransaction): ScheduledTransaction = api.addScheduledTransaction(transaction)
    override suspend fun updateScheduledTransaction(transaction: ScheduledTransaction): ScheduledTransaction {
        val transactionId = transaction.id ?: throw IllegalArgumentException("Scheduled transaction ID is required for update")
        return api.updateScheduledTransaction(transactionId, transaction)
    }
    override suspend fun deleteScheduledTransaction(id: String) = api.deleteScheduledTransaction(id)

    // Utility operations
    override suspend fun categorizeAllTransactions(referenceDate: String) = api.categorizeAllTransactions(referenceDate)
    override suspend fun categorizeUnbudgeted(referenceDate: String) = api.categorizeUnbudgeted(referenceDate)
    override suspend fun importTransactions(text: String, accountId: String, skipDuplicates: Boolean): List<String> =
        api.importTransactions(mapOf("text" to text, "accountId" to accountId, "skipDuplicates" to skipDuplicates))
    override suspend fun previewTransactionImport(text: String, accountId: String): TransactionImportPreview =
        api.previewTransactionImport(mapOf("text" to text, "accountId" to accountId))
} 