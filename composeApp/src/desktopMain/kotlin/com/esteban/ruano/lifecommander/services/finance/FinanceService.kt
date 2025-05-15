package services.finance

import com.lifecommander.finance.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.datetime.*
import kotlinx.serialization.json.Json
import services.auth.TokenStorage
import com.esteban.ruano.lifecommander.services.habits.appHeaders

class FinanceService(
    private val baseUrl: String,
    private val httpClient: HttpClient,
    private val tokenStorage: TokenStorage
) {
    // Transaction endpoints
    suspend fun getTransactions(
        startDate: LocalDateTime? = null,
        endDate: LocalDateTime? = null,
        category: Category? = null,
        accountId: String? = null
    ): List<Transaction> {
        val url = buildString {
            append("$baseUrl/finance/transactions")
            val params = mutableListOf<String>()
            startDate?.let { params.add("startDate=$it") }
            endDate?.let { params.add("endDate=$it") }
            category?.let { params.add("category=$it") }
            accountId?.let { params.add("accountId=$it") }
            if (params.isNotEmpty()) {
                append("?${params.joinToString("&")}")
            }
        }
        return httpClient.get(url) {
            appHeaders(tokenStorage.getToken())
        }.body()
    }

    suspend fun getTransaction(id: String): Transaction {
        return httpClient.get("$baseUrl/finance/transactions/$id") {
            appHeaders(tokenStorage.getToken())
        }.body()
    }

    suspend fun addTransaction(transaction: Transaction): Transaction {
        return httpClient.post("$baseUrl/finance/transactions") {
            contentType(ContentType.Application.Json)
            appHeaders(tokenStorage.getToken())
            setBody(transaction)
        }.body()
    }

    suspend fun updateTransaction(transaction: Transaction): Transaction {
        return httpClient.put("$baseUrl/finance/transactions/${transaction.id}") {
            contentType(ContentType.Application.Json)
            appHeaders(tokenStorage.getToken())
            setBody(transaction)
        }.body()
    }

    suspend fun deleteTransaction(id: String) {
        httpClient.delete("$baseUrl/finance/transactions/$id") {
            appHeaders(tokenStorage.getToken())
        }
    }

    // Account endpoints
    suspend fun getAccounts(): List<Account> {
        return httpClient.get("$baseUrl/finance/accounts") {
            appHeaders(tokenStorage.getToken())
        }.body()
    }

    suspend fun getAccount(id: String): Account {
        return httpClient.get("$baseUrl/finance/accounts/$id") {
            appHeaders(tokenStorage.getToken())
        }.body()
    }

    suspend fun getAccountBalance(id: String): Double {
        return httpClient.get("$baseUrl/finance/accounts/$id/balance") {
            appHeaders(tokenStorage.getToken())
        }.body<Map<String, Double>>()["balance"] ?: 0.0
    }

    suspend fun addAccount(account: Account): Account {
        return httpClient.post("$baseUrl/finance/accounts") {
            contentType(ContentType.Application.Json)
            appHeaders(tokenStorage.getToken())
            setBody(account)
        }.body()
    }

    suspend fun updateAccount(account: Account): Account {
        return httpClient.put("$baseUrl/finance/accounts/${account.id}") {
            contentType(ContentType.Application.Json)
            appHeaders(tokenStorage.getToken())
            setBody(account)
        }.body()
    }

    suspend fun deleteAccount(id: String) {
        httpClient.delete("$baseUrl/finance/accounts/$id") {
            appHeaders(tokenStorage.getToken())
        }
    }

    // Budget endpoints
    suspend fun getBudgets(): List<Budget> {
        return httpClient.get("$baseUrl/finance/budgets") {
            appHeaders(tokenStorage.getToken())
        }.body()
    }

    suspend fun getBudget(id: String): Budget {
        return httpClient.get("$baseUrl/finance/budgets/$id") {
            appHeaders(tokenStorage.getToken())
        }.body()
    }

    suspend fun getBudgetProgress(id: String): BudgetProgress {
        return httpClient.get("$baseUrl/finance/budgets/$id/progress") {
            appHeaders(tokenStorage.getToken())
        }.body()
    }

    suspend fun getOverBudgetCategories(): List<Category> {
        return httpClient.get("$baseUrl/finance/budgets/over-budget") {
            appHeaders(tokenStorage.getToken())
        }.body()
    }

    suspend fun addBudget(budget: Budget): Budget {
        return httpClient.post("$baseUrl/finance/budgets") {
            contentType(ContentType.Application.Json)
            appHeaders(tokenStorage.getToken())
            setBody(budget)
        }.body()
    }

    suspend fun updateBudget(budget: Budget): Budget {
        return httpClient.put("$baseUrl/finance/budgets/${budget.id}") {
            contentType(ContentType.Application.Json)
            appHeaders(tokenStorage.getToken())
            setBody(budget)
        }.body()
    }

    suspend fun deleteBudget(id: String) {
        httpClient.delete("$baseUrl/finance/budgets/$id") {
            appHeaders(tokenStorage.getToken())
        }
    }

    // Savings goal endpoints
    suspend fun getSavingsGoals(): List<SavingsGoal> {
        return httpClient.get("$baseUrl/finance/savings-goals") {
            appHeaders(tokenStorage.getToken())
        }.body()
    }

    suspend fun getSavingsGoal(id: String): SavingsGoal {
        return httpClient.get("$baseUrl/finance/savings-goals/$id") {
            appHeaders(tokenStorage.getToken())
        }.body()
    }

    suspend fun getSavingsGoalProgress(id: String): SavingsGoalProgress {
        return httpClient.get("$baseUrl/finance/savings-goals/$id/progress") {
            appHeaders(tokenStorage.getToken())
        }.body()
    }

    suspend fun addSavingsGoal(goal: SavingsGoal): SavingsGoal {
        return httpClient.post("$baseUrl/finance/savings-goals") {
            contentType(ContentType.Application.Json)
            appHeaders(tokenStorage.getToken())
            setBody(goal)
        }.body()
    }

    suspend fun updateSavingsGoal(goal: SavingsGoal): SavingsGoal {
        return httpClient.put("$baseUrl/finance/savings-goals/${goal.id}") {
            contentType(ContentType.Application.Json)
            appHeaders(tokenStorage.getToken())
            setBody(goal)
        }.body()
    }

    suspend fun deleteSavingsGoal(id: String) {
        httpClient.delete("$baseUrl/finance/savings-goals/$id") {
            appHeaders(tokenStorage.getToken())
        }
    }
} 