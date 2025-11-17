package com.esteban.ruano.finance_data.repository

import com.esteban.ruano.core.domain.preferences.Preferences
import com.esteban.ruano.core.helpers.NetworkHelper
import com.esteban.ruano.core_data.repository.BaseRepository
import com.esteban.ruano.finance_data.datasource.FinanceDataSource
import com.esteban.ruano.finance_domain.repository.FinanceRepository
import com.esteban.ruano.lifecommander.models.finance.*
import com.lifecommander.finance.model.*
import kotlinx.coroutines.flow.first

class FinanceRepositoryImpl(
    private val dataSource: FinanceDataSource,
    private val networkHelper: NetworkHelper,
    private val preferences: Preferences
) : BaseRepository(), FinanceRepository {

    override suspend fun getTransactions(
        filter: String?,
        page: Int?,
        limit: Int?,
        filters: TransactionFilters
    ): Result<TransactionsResponse> = doRequest(
        preferences.loadLastFetchTime().first(), networkHelper.isNetworkAvailable(),
        offlineModeEnabled = false,
        forceRefresh = false,
        localFetch = { throw NotImplementedError("No local data source implemented") },
        remoteFetch = {
            val pageSize = limit ?: 50
            val pageNumber = page ?: 0
            dataSource.getTransactions(
                limit = pageSize,
                page = pageNumber * pageSize, // Convert page number to offset
                filters = filters
            )
        }
    )

    override suspend fun getTransaction(transactionId: String): Result<Transaction> = doRequest(
        preferences.loadLastFetchTime().first(), networkHelper.isNetworkAvailable(),
        offlineModeEnabled = false,
        forceRefresh = false,
        localFetch = { throw NotImplementedError("No local data source implemented") },
        remoteFetch = {
            dataSource.getTransaction(transactionId)
        }
    )

    override suspend fun addTransaction(transaction: Transaction): Result<Transaction> = doRequest(
        preferences.loadLastFetchTime().first(), networkHelper.isNetworkAvailable(),
        offlineModeEnabled = false,
        forceRefresh = false,
        localFetch = { throw NotImplementedError("No local data source implemented") },
        remoteFetch = {
            dataSource.addTransaction(transaction)
        }
    )

    override suspend fun updateTransaction(transaction: Transaction): Result<Transaction> =
        doRequest(
            preferences.loadLastFetchTime().first(), networkHelper.isNetworkAvailable(),
            offlineModeEnabled = false,
            forceRefresh = false,
            localFetch = { throw NotImplementedError("No local data source implemented") },
            remoteFetch = {
                dataSource.updateTransaction(transaction)
            }
        )

    override suspend fun deleteTransaction(id: String): Result<Unit> = doRequest(
        preferences.loadLastFetchTime().first(), networkHelper.isNetworkAvailable(),
        offlineModeEnabled = false,
        forceRefresh = false,
        localFetch = { throw NotImplementedError("No local data source implemented") },
        remoteFetch = {
            dataSource.deleteTransaction(id)
        }
    )

    override suspend fun getAccounts(): Result<List<Account>> = doRequest(
        preferences.loadLastFetchTime().first(), networkHelper.isNetworkAvailable(),
        offlineModeEnabled = false,
        forceRefresh = false,
        localFetch = { throw NotImplementedError("No local data source implemented") },
        remoteFetch = {
            dataSource.getAccounts()
        }
    )

    override suspend fun addAccount(account: Account): Result<Account> = doRequest(
        preferences.loadLastFetchTime().first(), networkHelper.isNetworkAvailable(),
        offlineModeEnabled = false,
        forceRefresh = false,
        localFetch = { throw NotImplementedError("No local data source implemented") },
        remoteFetch = {
            dataSource.addAccount(account)
        }
    )

    override suspend fun updateAccount(account: Account): Result<Account> = doRequest(
        preferences.loadLastFetchTime().first(), networkHelper.isNetworkAvailable(),
        offlineModeEnabled = false,
        forceRefresh = false,
        localFetch = { throw NotImplementedError("No local data source implemented") },
        remoteFetch = {
            dataSource.updateAccount(account)
        }
    )

    override suspend fun deleteAccount(id: String): Result<Unit> = doRequest(
        preferences.loadLastFetchTime().first(), networkHelper.isNetworkAvailable(),
        offlineModeEnabled = false,
        forceRefresh = false,
        localFetch = { throw NotImplementedError("No local data source implemented") },
        remoteFetch = {
            dataSource.deleteAccount(id)
        }
    )

    override suspend fun getBudgets(
        filters: BudgetFilters,
        referenceDate: String
    ): Result<List<BudgetProgress>> = doRequest(
        preferences.loadLastFetchTime().first(), networkHelper.isNetworkAvailable(),
        offlineModeEnabled = false,
        forceRefresh = false,
        localFetch = { throw NotImplementedError("No local data source implemented") },
        remoteFetch = {
            dataSource.getBudgets(filters, referenceDate)
        }
    )

    override suspend fun addBudget(budget: Budget): Result<Budget> = doRequest(
        preferences.loadLastFetchTime().first(), networkHelper.isNetworkAvailable(),
        offlineModeEnabled = false,
        forceRefresh = false,
        localFetch = { throw NotImplementedError("No local data source implemented") },
        remoteFetch = {
            dataSource.addBudget(budget)
        }
    )

    override suspend fun updateBudget(budget: Budget): Result<Budget> = doRequest(
        preferences.loadLastFetchTime().first(), networkHelper.isNetworkAvailable(),
        offlineModeEnabled = false,
        forceRefresh = false,
        localFetch = { throw NotImplementedError("No local data source implemented") },
        remoteFetch = {
            dataSource.updateBudget(budget)
        }
    )

    override suspend fun deleteBudget(id: String): Result<Unit> = doRequest(
        preferences.loadLastFetchTime().first(), networkHelper.isNetworkAvailable(),
        offlineModeEnabled = false,
        forceRefresh = false,
        localFetch = { throw NotImplementedError("No local data source implemented") },
        remoteFetch = {
            dataSource.deleteBudget(id)
        }
    )

    override suspend fun getBudgetProgress(budgetId: String): Result<BudgetProgress> = doRequest(
        preferences.loadLastFetchTime().first(), networkHelper.isNetworkAvailable(),
        offlineModeEnabled = false,
        forceRefresh = false,
        localFetch = { throw NotImplementedError("No local data source implemented") },
        remoteFetch = {
            dataSource.getBudgetProgress(budgetId)
        }
    )

    override suspend fun getBudgetTransactions(
        budgetId: String,
        referenceDate: String,
        filters: TransactionFilters
    ): Result<List<Transaction>> = doRequest(
        preferences.loadLastFetchTime().first(), networkHelper.isNetworkAvailable(),
        offlineModeEnabled = false,
        forceRefresh = false,
        localFetch = { throw NotImplementedError("No local data source implemented") },
        remoteFetch = {
            dataSource.getBudgetTransactions(budgetId, referenceDate, filters)
        }
    )

    override suspend fun getSavingsGoals(): Result<List<SavingsGoal>> = doRequest(
        preferences.loadLastFetchTime().first(), networkHelper.isNetworkAvailable(),
        offlineModeEnabled = false,
        forceRefresh = false,
        localFetch = { throw NotImplementedError("No local data source implemented") },
        remoteFetch = {
            dataSource.getSavingsGoals()
        }
    )

    override suspend fun addSavingsGoal(goal: SavingsGoal): Result<SavingsGoal> = doRequest(
        preferences.loadLastFetchTime().first(), networkHelper.isNetworkAvailable(),
        offlineModeEnabled = false,
        forceRefresh = false,
        localFetch = { throw NotImplementedError("No local data source implemented") },
        remoteFetch = {
            dataSource.addSavingsGoal(goal)
        }
    )

    override suspend fun updateSavingsGoal(goal: SavingsGoal): Result<SavingsGoal> = doRequest(
        preferences.loadLastFetchTime().first(), networkHelper.isNetworkAvailable(),
        offlineModeEnabled = false,
        forceRefresh = false,
        localFetch = { throw NotImplementedError("No local data source implemented") },
        remoteFetch = {
            dataSource.updateSavingsGoal(goal)
        }
    )

    override suspend fun deleteSavingsGoal(id: String): Result<Unit> = doRequest(
        preferences.loadLastFetchTime().first(), networkHelper.isNetworkAvailable(),
        offlineModeEnabled = false,
        forceRefresh = false,
        localFetch = { throw NotImplementedError("No local data source implemented") },
        remoteFetch = {
            dataSource.deleteSavingsGoal(id)
        }
    )

    override suspend fun getSavingsGoalProgress(goalId: String): Result<SavingsGoalProgress> =
        doRequest(
            preferences.loadLastFetchTime().first(), networkHelper.isNetworkAvailable(),
            offlineModeEnabled = false,
            forceRefresh = false,
            localFetch = { throw NotImplementedError("No local data source implemented") },
            remoteFetch = {
                dataSource.getSavingsGoalProgress(goalId)
            }
        )

    override suspend fun getScheduledTransactions(
        filter: String?,
        page: Int?,
        limit: Int?,
        filters: TransactionFilters
    ): Result<ScheduledTransactionsResponse> = doRequest(
        preferences.loadLastFetchTime().first(), networkHelper.isNetworkAvailable(),
        offlineModeEnabled = false,
        forceRefresh = false,
        localFetch = { throw NotImplementedError("No local data source implemented") },
        remoteFetch = {
            val pageSize = limit ?: 50
            val pageNumber = page ?: 0
            dataSource.getScheduledTransactions(
                limit = pageSize,
                page = pageNumber * pageSize, // Convert page number to offset
                filters = filters
            )
        }
    )

    override suspend fun addScheduledTransaction(transaction: ScheduledTransaction): Result<ScheduledTransaction> =
        doRequest(
            preferences.loadLastFetchTime().first(), networkHelper.isNetworkAvailable(),
            offlineModeEnabled = false,
            forceRefresh = false,
            localFetch = { throw NotImplementedError("No local data source implemented") },
            remoteFetch = {
                dataSource.addScheduledTransaction(transaction)
            }
        )

    override suspend fun updateScheduledTransaction(transaction: ScheduledTransaction): Result<ScheduledTransaction> =
        doRequest(
            preferences.loadLastFetchTime().first(), networkHelper.isNetworkAvailable(),
            offlineModeEnabled = false,
            forceRefresh = false,
            localFetch = { throw NotImplementedError("No local data source implemented") },
            remoteFetch = {
                dataSource.updateScheduledTransaction(transaction)
            }
        )

    override suspend fun deleteScheduledTransaction(id: String): Result<Unit> = doRequest(
        preferences.loadLastFetchTime().first(), networkHelper.isNetworkAvailable(),
        offlineModeEnabled = false,
        forceRefresh = false,
        localFetch = { throw NotImplementedError("No local data source implemented") },
        remoteFetch = {
            dataSource.deleteScheduledTransaction(id)
        }
    )

    override suspend fun importTransactions(
        text: String,
        accountId: String,
        skipDuplicates: Boolean
    ): Result<List<String>> = doRequest(
        preferences.loadLastFetchTime().first(), networkHelper.isNetworkAvailable(),
        offlineModeEnabled = false,
        forceRefresh = false,
        localFetch = { throw NotImplementedError("No local data source implemented") },
        remoteFetch = {
            dataSource.importTransactions(text, accountId, skipDuplicates)
        }
    )

    override suspend fun previewTransactionImport(
        text: String,
        accountId: String
    ): Result<TransactionImportPreview> = doRequest(
        preferences.loadLastFetchTime().first(), networkHelper.isNetworkAvailable(),
        offlineModeEnabled = false,
        forceRefresh = false,
        localFetch = { throw NotImplementedError("No local data source implemented") },
        remoteFetch = {
            dataSource.previewTransactionImport(text, accountId)
        }
    )

    override suspend fun categorizeAllTransactions(referenceDate: String): Result<Unit> = doRequest(
        preferences.loadLastFetchTime().first(), networkHelper.isNetworkAvailable(),
        offlineModeEnabled = false,
        forceRefresh = false,
        localFetch = { throw NotImplementedError("No local data source implemented") },
        remoteFetch = {
            dataSource.categorizeAllTransactions(referenceDate)
        }
    )

    override suspend fun categorizeUnbudgeted(referenceDate: String): Result<Unit> = doRequest(
        preferences.loadLastFetchTime().first(), networkHelper.isNetworkAvailable(),
        offlineModeEnabled = false,
        forceRefresh = false,
        localFetch = { throw NotImplementedError("No local data source implemented") },
        remoteFetch = {
            dataSource.categorizeUnbudgeted(referenceDate)
        }
    )
} 