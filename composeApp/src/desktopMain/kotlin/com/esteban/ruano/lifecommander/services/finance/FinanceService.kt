package com.esteban.ruano.lifecommander.services.finance

// ---------- Paging imports ----------
import androidx.paging.PagingData
import com.esteban.ruano.lifecommander.models.finance.*
import com.esteban.ruano.lifecommander.utils.appHeaders
import com.esteban.ruano.lifecommander.utils.buildParametersString
import com.esteban.ruano.utils.DateUIUtils.formatDefault
import com.esteban.ruano.utils.DateUIUtils.getCurrentDateTime
import com.lifecommander.finance.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.TimeZone
import offsetPager
import services.auth.TokenStorageImpl

class FinanceService(
    private val baseUrl: String,
    private val httpClient: HttpClient,
    private val tokenStorageImpl: TokenStorageImpl
) {
    // ===================== Transactions =====================

    suspend fun getTransactions(
        limit: Int = 20,
        offset: Int = 0,
        withFutureTransactions: Boolean = false,
        filters: TransactionFilters = TransactionFilters()
    ): TransactionsResponse {
        val url = buildString {
            append("$baseUrl/finance/transactions")
            val params = mutableListOf<String>()

            if (withFutureTransactions) {
                params.add(
                    "scheduledBaseDate=${
                        getCurrentDateTime(TimeZone.currentSystemDefault()).date.formatDefault()
                    }"
                )
            }

            params.add("limit=$limit")
            params.add("offset=$offset")

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

    // Paged facade (kept for symmetry with others)
    suspend fun getTransactionsPaged(
        limit: Int,
        offset: Int,
        withFutureTransactions: Boolean = false,
        filters: TransactionFilters = TransactionFilters()
    ): TransactionsResponse =
        getTransactions(limit, offset, withFutureTransactions, filters)

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
        val response = httpClient.patch("$baseUrl/finance/transactions/${transaction.id}") {
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

    // ===================== Accounts =====================

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

    // ===================== Budgets =====================

    suspend fun getBudgets(): List<Budget> {
        return httpClient.get("$baseUrl/finance/budgets") {
            appHeaders(tokenStorageImpl.getToken())
        }.body()
    }

    // Paged HTTP helper for budgets (limit/offset)
    suspend fun getBudgetsPaged(limit: Int, offset: Int): List<Budget> {
        val url = "$baseUrl/finance/budgets?limit=$limit&offset=$offset"
        return httpClient.get(url) { appHeaders(tokenStorageImpl.getToken()) }.body()
    }

    suspend fun getBudgetsWithProgress(
        filters: BudgetFilters = BudgetFilters(),
        referenceDate: String,
        limit: Int, offset: Int
    ): List<BudgetProgress> {
        val url = buildString {
            append("$baseUrl/finance/budgets/withProgress")
            val params = mutableListOf<String>()

            params.add("referenceDate=$referenceDate")
            params.add("limit=$limit")
            params.add("offset=$offset")

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

    // Paged HTTP helper for budgets with progress (limit/offset)
    suspend fun getBudgetsWithProgressPaged(
        referenceDate: String,
        filters: BudgetFilters = BudgetFilters(),
        limit: Int,
        offset: Int
    ): List<BudgetProgress> {
        val url = buildString {
            append("$baseUrl/finance/budgets/withProgress")
            val params = mutableListOf(
                "referenceDate=$referenceDate",
                "limit=$limit",
                "offset=$offset"
            )
            filters.searchPattern?.let { params += "search=$it" }
            filters.categories?.forEach { params += "category=$it" }
            filters.minAmount?.let { params += "minAmount=$it" }
            filters.maxAmount?.let { params += "maxAmount=$it" }
            filters.isOverBudget?.let { params += "isOverBudget=$it" }
            append("?${params.joinToString("&")}")
        }
        return httpClient.get(url) { appHeaders(tokenStorageImpl.getToken()) }.body()
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

    // ===================== Savings goals =====================

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

    // ===================== Import / Preview =====================

    suspend fun importTransactions(text: String, accountId: String, skipDuplicates: Boolean): List<String> {
        return httpClient.post("$baseUrl/finance/transactions/import") {
            contentType(ContentType.Application.Json)
            appHeaders(tokenStorageImpl.getToken())
            setBody(
                mapOf(
                    "text" to text,
                    "accountId" to accountId,
                    "skipDuplicates" to skipDuplicates
                )
            )
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

    // ===================== Budget Transactions =====================

    suspend fun getBudgetTransactions(
        budgetId: String,
        referenceDate: String,
        filters: TransactionFilters = TransactionFilters(),
        limit: Int,
        offset: Int,
    ): List<Transaction> {
        val url = buildString {
            append("$baseUrl/finance/budgets/$budgetId/transactions")
            val params = mutableListOf<String>()

            params.add("referenceDate=$referenceDate")
            params.add("limit=$limit")
            params.add("offset=$offset")

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

    // Paged HTTP helper for budget transactions
    suspend fun getBudgetTransactionsPaged(
        budgetId: String,
        referenceDate: String,
        filters: TransactionFilters = TransactionFilters(),
        limit: Int,
        offset: Int
    ): List<Transaction> {
        val url = buildString {
            append("$baseUrl/finance/budgets/$budgetId/transactions")
            val params = mutableListOf("referenceDate=$referenceDate", "limit=$limit", "offset=$offset")
            filters.buildParametersString()?.let { params += it }
            append("?${params.joinToString("&")}")
        }
        return httpClient.get(url) { appHeaders(tokenStorageImpl.getToken()) }.body()
    }

    // ===================== Unbudgeted Transactions =====================

    suspend fun categorizeUnbudgeted(referenceDate: String): Int {
        val url = buildString {
            append("$baseUrl/finance/budgets/unbudgeted/categorize")
            val params = mutableListOf<String>()
            params.add("referenceDate=$referenceDate")
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
            params.add("referenceDate=$referenceDate")
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

    // Paged HTTP helper for unbudgeted transactions list
    suspend fun getUnbudgetedTransactionsPaged(
        referenceDate: String,
        filters: TransactionFilters = TransactionFilters(),
        limit: Int,
        offset: Int
    ): List<Transaction> {
        val url = buildString {
            append("$baseUrl/finance/budgets/unbudgeted/transactions")
            val params = mutableListOf("referenceDate=$referenceDate", "limit=$limit", "offset=$offset")
            filters.buildParametersString()?.let { params += it }
            append("?${params.joinToString("&")}")
        }
        return httpClient.get(url) { appHeaders(tokenStorageImpl.getToken()) }.body()
    }

    // ===================== Scheduled Transactions =====================

    suspend fun getScheduledTransactions(
        limit: Int = 50,
        offset: Int = 0,
        filters: TransactionFilters = TransactionFilters()
    ): ScheduledTransactionsResponse {
        val url = buildString {
            append("$baseUrl/finance/scheduled-transactions")
            val params = mutableListOf<String>()

            params.add("limit=$limit")
            params.add("offset=$offset")

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

    suspend fun addScheduledTransaction(transaction: ScheduledTransaction) {
        val response = httpClient.post("$baseUrl/finance/scheduled-transactions") {
            contentType(ContentType.Application.Json)
            appHeaders(tokenStorageImpl.getToken())
            setBody(transaction)
        }

        if (response.status != HttpStatusCode.Created) {
            throw Exception("Failed to add scheduled transaction: ${response.status}")
        }
    }

    suspend fun updateScheduledTransaction(transaction: ScheduledTransaction) {
        val response = httpClient.patch("$baseUrl/finance/scheduled-transactions/${transaction.id}") {
            contentType(ContentType.Application.Json)
            appHeaders(tokenStorageImpl.getToken())
            setBody(transaction)
        }
        if (response.status != HttpStatusCode.OK) {
            throw Exception("Failed to update scheduled transaction: ${response.status}")
        }
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

    // ===================== Concrete Pager facades using the generic source =====================

    fun pagedBudgets(pageSize: Int = 20): Flow<PagingData<Budget>> =
        offsetPager(pageSize) { limit, offset ->
            getBudgetsPaged(limit, offset)
        }

    fun pagedBudgetsWithProgress(
        referenceDate: String,
        filters: BudgetFilters = BudgetFilters(),
        pageSize: Int = 20
    ): Flow<PagingData<BudgetProgress>> =
        offsetPager(pageSize) { limit, offset ->
            getBudgetsWithProgressPaged(referenceDate, filters, limit, offset)
        }

    fun pagedTransactions(
        withFutureTransactions: Boolean = false,
        filters: TransactionFilters = TransactionFilters(),
        pageSize: Int = 20
    ): Flow<PagingData<Transaction>> =
        offsetPager(pageSize) { limit, offset ->
            getTransactionsPaged(limit, offset, withFutureTransactions, filters).transactions
        }

    fun pagedBudgetTransactions(
        budgetId: String,
        referenceDate: String,
        filters: TransactionFilters = TransactionFilters(),
        pageSize: Int = 20
    ): Flow<PagingData<Transaction>> =
        offsetPager(pageSize) { limit, offset ->
            getBudgetTransactionsPaged(budgetId, referenceDate, filters, limit, offset)
        }

    fun pagedUnbudgetedTransactions(
        referenceDate: String,
        filters: TransactionFilters = TransactionFilters(),
        pageSize: Int = 20
    ): Flow<PagingData<Transaction>> =
        offsetPager(pageSize) { limit, offset ->
            getUnbudgetedTransactionsPaged(referenceDate, filters, limit, offset)
        }

    fun pagedScheduledTransactions(
        filters: TransactionFilters = TransactionFilters(),
        pageSize: Int = 20
    ): Flow<PagingData<ScheduledTransaction>> =
        offsetPager(pageSize) { limit, offset ->
            getScheduledTransactions(limit, offset, filters).transactions
        }
}
