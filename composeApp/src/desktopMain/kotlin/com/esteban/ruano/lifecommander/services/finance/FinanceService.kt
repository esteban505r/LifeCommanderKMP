package com.esteban.ruano.lifecommander.services.finance

import com.esteban.ruano.lifecommander.models.finance.Budget
import com.esteban.ruano.lifecommander.models.finance.BudgetFilters
import com.esteban.ruano.lifecommander.models.finance.BudgetProgress
import com.esteban.ruano.lifecommander.models.finance.Category
import com.esteban.ruano.lifecommander.models.finance.ScheduledTransactionsResponse
import com.esteban.ruano.lifecommander.models.finance.TransactionFilters
import com.esteban.ruano.lifecommander.models.finance.TransactionsResponse
import com.esteban.ruano.lifecommander.utils.appHeaders
import com.esteban.ruano.lifecommander.utils.buildParametersString
import com.esteban.ruano.utils.DateUIUtils.formatDefault
import com.esteban.ruano.utils.DateUIUtils.getCurrentDateTime
import com.lifecommander.finance.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.datetime.TimeZone
import services.auth.TokenStorageImpl




class FinanceService(
    private val baseUrl: String,
    private val httpClient: HttpClient,
    private val tokenStorageImpl: TokenStorageImpl
) {
    suspend fun getTransactions(
        limit: Int = 50,
        offset: Int = 0,
        withFutureTransactions: Boolean = false,
        filters: TransactionFilters = TransactionFilters()
    ): TransactionsResponse {
        val url = buildString {
            append("$baseUrl/finance/transactions")
            val params = mutableListOf<String>()

            if(withFutureTransactions) {
                params.add("scheduledBaseDate=${getCurrentDateTime(
                    TimeZone.currentSystemDefault()
                ).date.formatDefault()}")
            }

            params.add("limit=$limit")
            params.add("offset=$offset")
            
            // Add filter parameters
           val filterParams = filters.buildParametersString()
            
            if (params.isNotEmpty()) {
                append("?${params.joinToString("&")}")
            }

            if (filterParams != null) {
                append("&$filterParams")
            }
        }
        
        return httpClient.get(url) {
            appHeaders(tokenStorageImpl.getToken())
        }.body()
    }

    suspend fun getTransaction(id: String): Transaction {
        return httpClient.get("$baseUrl/finance/transactions/$id") {
            appHeaders(tokenStorageImpl.getToken())
        }.body()
    }

    suspend fun addTransaction(transaction: Transaction) {
        val response = httpClient.post("$baseUrl/finance/transactions") {
            contentType(ContentType.Application.Json)
            appHeaders(tokenStorageImpl.getToken())
            setBody(transaction)
        }
        if (response.status != HttpStatusCode.Created) {
            throw Exception("Failed to add transaction: ${response.status}")
        }
    }

    suspend fun updateTransaction(transaction: Transaction) {
        val response =  httpClient.patch("$baseUrl/finance/transactions/${transaction.id}") {
            contentType(ContentType.Application.Json)
            appHeaders(tokenStorageImpl.getToken())
            setBody(transaction)
        }
        if (response.status != HttpStatusCode.OK) {
            throw Exception("Failed to update transaction: ${response.status}")
        }
    }

    suspend fun deleteTransaction(id: String) {
        httpClient.delete("$baseUrl/finance/transactions/$id") {
            appHeaders(tokenStorageImpl.getToken())
        }
    }

    // Account endpoints
    suspend fun getAccounts(): List<Account> {
        return httpClient.get("$baseUrl/finance/accounts") {
            appHeaders(tokenStorageImpl.getToken())
        }.body()
    }

    suspend fun getAccount(id: String): Account {
        return httpClient.get("$baseUrl/finance/accounts/$id") {
            appHeaders(tokenStorageImpl.getToken())
        }.body()
    }

    suspend fun getAccountBalance(id: String): Double {
        return httpClient.get("$baseUrl/finance/accounts/$id/balance") {
            appHeaders(tokenStorageImpl.getToken())
        }.body<Map<String, Double>>()["balance"] ?: 0.0
    }

    suspend fun addAccount(account: Account): Account {
        return httpClient.post("$baseUrl/finance/accounts") {
            contentType(ContentType.Application.Json)
            appHeaders(tokenStorageImpl.getToken())
            setBody(account)
        }.body()
    }

    suspend fun updateAccount(account: Account): Account {
        return httpClient.patch("$baseUrl/finance/accounts/${account.id}") {
            contentType(ContentType.Application.Json)
            appHeaders(tokenStorageImpl.getToken())
            setBody(account)
        }.body()
    }

    suspend fun deleteAccount(id: String) {
        httpClient.delete("$baseUrl/finance/accounts/$id") {
            appHeaders(tokenStorageImpl.getToken())
        }
    }

    // Budget endpoints
    suspend fun getBudgets(): List<Budget> {
        return httpClient.get("$baseUrl/finance/budgets") {
            appHeaders(tokenStorageImpl.getToken())
        }.body()
    }

    suspend fun getBudgetsWithProgress(
        filters: BudgetFilters = BudgetFilters(),
        referenceDate: String,
    ): List<BudgetProgress> {
        val url = buildString {
            append("$baseUrl/finance/budgets/withProgress")
            val params = mutableListOf<String>()

            params.add("referenceDate=${referenceDate}")
            
            // Add filter parameters
            filters.searchPattern?.let { params.add("search=$it") }
            filters.categories?.forEach { params.add("category=$it") }
            filters.minAmount?.let { params.add("minAmount=$it") }
            filters.maxAmount?.let { params.add("maxAmount=$it") }
            filters.isOverBudget?.let { params.add("isOverBudget=$it") }
            
            if (params.isNotEmpty()) {
                append("?${params.joinToString("&")}")
            }
        }
        
        return httpClient.get(url) {
            appHeaders(tokenStorageImpl.getToken())
        }.body()
    }

    suspend fun getBudget(id: String): Budget {
        return httpClient.get("$baseUrl/finance/budgets/$id") {
            appHeaders(tokenStorageImpl.getToken())
        }.body()
    }

    suspend fun getBudgetProgress(id: String): BudgetProgress {
        return httpClient.get("$baseUrl/finance/budgets/$id/progress") {
            appHeaders(tokenStorageImpl.getToken())
        }.body()
    }

    suspend fun getOverBudgetCategories(): List<Category> {
        return httpClient.get("$baseUrl/finance/budgets/over-budget") {
            appHeaders(tokenStorageImpl.getToken())
        }.body()
    }

    suspend fun addBudget(budget: Budget): Budget {
        return httpClient.post("$baseUrl/finance/budgets") {
            contentType(ContentType.Application.Json)
            appHeaders(tokenStorageImpl.getToken())
            setBody(budget)
        }.body()
    }

    suspend fun updateBudget(budget: Budget): Budget {
        return httpClient.patch("$baseUrl/finance/budgets/${budget.id}") {
            contentType(ContentType.Application.Json)
            appHeaders(tokenStorageImpl.getToken())
            setBody(budget)
        }.body()
    }

    suspend fun deleteBudget(id: String) {
        httpClient.delete("$baseUrl/finance/budgets/$id") {
            appHeaders(tokenStorageImpl.getToken())
        }
    }

    // Savings goal endpoints
    suspend fun getSavingsGoals(): List<SavingsGoal> {
        return httpClient.get("$baseUrl/finance/savings-goals") {
            appHeaders(tokenStorageImpl.getToken())
        }.body()
    }

    suspend fun getSavingsGoal(id: String): SavingsGoal {
        return httpClient.get("$baseUrl/finance/savings-goals/$id") {
            appHeaders(tokenStorageImpl.getToken())
        }.body()
    }

    suspend fun getSavingsGoalProgress(id: String): SavingsGoalProgress {
        return httpClient.get("$baseUrl/finance/savings-goals/$id/progress") {
            appHeaders(tokenStorageImpl.getToken())
        }.body()
    }

    suspend fun addSavingsGoal(goal: SavingsGoal): SavingsGoal {
        return httpClient.post("$baseUrl/finance/savings-goals") {
            contentType(ContentType.Application.Json)
            appHeaders(tokenStorageImpl.getToken())
            setBody(goal)
        }.body()
    }

    suspend fun updateSavingsGoal(goal: SavingsGoal): SavingsGoal {
        return httpClient.patch("$baseUrl/finance/savings-goals/${goal.id}") {
            contentType(ContentType.Application.Json)
            appHeaders(tokenStorageImpl.getToken())
            setBody(goal)
        }.body()
    }

    suspend fun deleteSavingsGoal(id: String) {
        httpClient.delete("$baseUrl/finance/savings-goals/$id") {
            appHeaders(tokenStorageImpl.getToken())
        }
    }

    suspend fun importTransactions(text: String, accountId: String, skipDuplicates: Boolean): List<String> {
        return httpClient.post("$baseUrl/finance/transactions/import") {
            contentType(ContentType.Application.Json)
            appHeaders(tokenStorageImpl.getToken())
            setBody(mapOf(
                "text" to text,
                "accountId" to accountId,
                "skipDuplicates" to skipDuplicates
            ))
        }.body<ImportTransactionsResponse>().transactionIds
    }

    suspend fun previewTransactionImport(text: String, accountId: String): TransactionImportPreview {
        val response = httpClient.post("$baseUrl/finance/transactions/import/preview") {
            contentType(ContentType.Application.Json)
            appHeaders(tokenStorageImpl.getToken())
            setBody(TransactionImportPreviewRequest(text, accountId))
        }
        
        return response.body()
    }

    suspend fun getBudgetTransactions(budgetId: String, referenceDate: String, filters: TransactionFilters = TransactionFilters()): List<Transaction> {
        val url = buildString {
            append("$baseUrl/finance/budgets/$budgetId/transactions")
            val params = mutableListOf<String>()

            // Add reference date parameter
            params.add("referenceDate=${referenceDate}")
            
            // Add filter parameters
            val filterParams = filters.buildParametersString()

            if (params.isNotEmpty()) {
                append("?${params.joinToString("&")}")
            }
            if (filterParams != null) {
                append("&$filterParams")
            }
        }
        val response = httpClient.get(url) {
            appHeaders(tokenStorageImpl.getToken())
        }

        if (response.status == HttpStatusCode.OK) {
            return response.body<List<Transaction>>()
        } else {
            throw Exception("Failed to fetch budget transactions: ${response.status}")
        }
    }

    suspend fun categorizeUnbudgeted(referenceDate: String): Int {
        val url = buildString {
            append("$baseUrl/finance/budgets/unbudgeted/categorize")
            val params = mutableListOf<String>()
            params.add("referenceDate=${referenceDate}")
            if (params.isNotEmpty()) {
                append("?${params.joinToString("&")}")
            }
        }
        
        val response = httpClient.post(url) {
            appHeaders(tokenStorageImpl.getToken())
        }

        if (response.status == HttpStatusCode.OK) {
            return response.body<Map<String, Int>>()["categorizedCount"] ?: 0
        } else {
            throw Exception("Failed to categorize unbudgeted transactions: ${response.status}")
        }
    }

    suspend fun categorizeAllTransactions(referenceDate: String): Int {
        val url = buildString {
            append("$baseUrl/finance/budgets/transactions/categorize")
            val params = mutableListOf<String>()
            params.add("referenceDate=${referenceDate}")
            if (params.isNotEmpty()) {
                append("?${params.joinToString("&")}")
            }
        }
        
        val response = httpClient.post(url) {
            appHeaders(tokenStorageImpl.getToken())
        }

        if (response.status == HttpStatusCode.OK) {
            return response.body<Map<String, Int>>()["categorizedCount"] ?: 0
        } else {
            throw Exception("Failed to categorize transactions: ${response.status}")
        }
    }

    // Scheduled Transactions endpoints
    suspend fun getScheduledTransactions(
        limit: Int = 50,
        offset: Int = 0,
        filters: TransactionFilters = TransactionFilters()
    ): ScheduledTransactionsResponse {
        val url = buildString {
            append("$baseUrl/finance/scheduled-transactions")
            val params = mutableListOf<String>()
            
            // Add pagination parameters
            params.add("limit=$limit")
            params.add("offset=$offset")
            
            // Add filter parameters
            val filterParams = filters.buildParametersString()
            
            if (params.isNotEmpty()) {
                append("?${params.joinToString("&")}")
            }

            if (filterParams != null) {
                append("&$filterParams")
            }
        }
        
        return httpClient.get(url) {
            appHeaders(tokenStorageImpl.getToken())
        }.body()
    }

    suspend fun getScheduledTransaction(id: String): ScheduledTransaction {
        return httpClient.get("$baseUrl/finance/scheduled-transactions/$id") {
            appHeaders(tokenStorageImpl.getToken())
        }.body()
    }

    suspend fun addScheduledTransaction(transaction: ScheduledTransaction): ScheduledTransaction {
        return httpClient.post("$baseUrl/finance/scheduled-transactions") {
            contentType(ContentType.Application.Json)
            appHeaders(tokenStorageImpl.getToken())
            setBody(transaction)
        }.body()
    }

    suspend fun updateScheduledTransaction(transaction: ScheduledTransaction): ScheduledTransaction {
        return httpClient.patch("$baseUrl/finance/scheduled-transactions/${transaction.id}") {
            contentType(ContentType.Application.Json)
            appHeaders(tokenStorageImpl.getToken())
            setBody(transaction)
        }.body()
    }

    suspend fun deleteScheduledTransaction(id: String) {
        httpClient.delete("$baseUrl/finance/scheduled-transactions/$id") {
            appHeaders(tokenStorageImpl.getToken())
        }
    }

    suspend fun getScheduledTransactionsByAccount(accountId: String): List<ScheduledTransaction> {
        return httpClient.get("$baseUrl/finance/scheduled-transactions/byAccount/$accountId") {
            appHeaders(tokenStorageImpl.getToken())
        }.body()
    }
}

