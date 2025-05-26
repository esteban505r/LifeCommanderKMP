package com.esteban.ruano.finance_data.remote

import com.esteban.ruano.lifecommander.models.finance.*
import com.lifecommander.finance.model.*
import retrofit2.http.*

interface FinanceApi {
    // Transactions
    @GET("finance/transactions")
    suspend fun getTransactions(
        @Query("limit") limit: Int? = 50,
        @Query("offset") offset: Int? = 0,
        @QueryMap filters: Map<String, @JvmSuppressWildcards Any?> = emptyMap()
    ): TransactionsResponse

    @GET("finance/transactions/{id}")
    suspend fun getTransaction(@Path("id") id: String): Transaction

    @POST("finance/transactions")
    suspend fun addTransaction(@Body transaction: Transaction): Transaction

    @PATCH("finance/transactions/{id}")
    suspend fun updateTransaction(@Path("id") id: String, @Body transaction: Transaction): Transaction

    @DELETE("finance/transactions/{id}")
    suspend fun deleteTransaction(@Path("id") id: String)

    // Accounts
    @GET("finance/accounts")
    suspend fun getAccounts(): List<Account>

    @POST("finance/accounts")
    suspend fun addAccount(@Body account: Account): Account

    @PATCH("finance/accounts/{id}")
    suspend fun updateAccount(@Path("id") id: String, @Body account: Account): Account

    @DELETE("finance/accounts/{id}")
    suspend fun deleteAccount(@Path("id") id: String)

    // Budgets
    @GET("finance/budgets/withProgress")
    suspend fun getBudgetsWithProgress(
        @QueryMap filters: Map<String, Any?> = emptyMap(),
        @Query("referenceDate") referenceDate: String
    ): List<BudgetProgress>

    @POST("finance/budgets")
    suspend fun addBudget(@Body budget: Budget): Budget

    @PATCH("finance/budgets/{id}")
    suspend fun updateBudget(@Path("id") id: String, @Body budget: Budget): Budget

    @DELETE("finance/budgets/{id}")
    suspend fun deleteBudget(@Path("id") id: String)

    @GET("finance/budgets/{id}/progress")
    suspend fun getBudgetProgress(@Path("id") id: String): BudgetProgress

    @GET("finance/budgets/{id}/transactions")
    suspend fun getBudgetTransactions(
        @Path("id") id: String,
        @Query("referenceDate") referenceDate: String,
        @QueryMap filters: Map<String, @JvmSuppressWildcards Any?> = emptyMap()
    ): List<Transaction>

    // Savings Goals
    @GET("finance/savings-goals")
    suspend fun getSavingsGoals(): List<SavingsGoal>

    @POST("finance/savings-goals")
    suspend fun addSavingsGoal(@Body goal: SavingsGoal): SavingsGoal

    @PATCH("finance/savings-goals/{id}")
    suspend fun updateSavingsGoal(@Path("id") id: String, @Body goal: SavingsGoal): SavingsGoal

    @DELETE("finance/savings-goals/{id}")
    suspend fun deleteSavingsGoal(@Path("id") id: String)

    @GET("finance/savings-goals/{id}/progress")
    suspend fun getSavingsGoalProgress(@Path("id") id: String): SavingsGoalProgress

    // Scheduled Transactions
    @GET("finance/scheduled-transactions")
    suspend fun getScheduledTransactions(
        @Query("limit") limit: Int? = 50,
        @Query("offset") offset: Int? = 0,
        @QueryMap filters: Map<String, @JvmSuppressWildcards Any?> = emptyMap()
    ): ScheduledTransactionsResponse

    @POST("finance/scheduled-transactions")
    suspend fun addScheduledTransaction(@Body transaction: ScheduledTransaction): ScheduledTransaction

    @PATCH("finance/scheduled-transactions/{id}")
    suspend fun updateScheduledTransaction(@Path("id") id: String, @Body transaction: ScheduledTransaction): ScheduledTransaction

    @DELETE("finance/scheduled-transactions/{id}")
    suspend fun deleteScheduledTransaction(@Path("id") id: String)

    // Import & Categorize
    @POST("finance/transactions/import")
    suspend fun importTransactions(@Body body: Map<String, @JvmSuppressWildcards Any?>): List<String>

    @POST("finance/transactions/import/preview")
    suspend fun previewTransactionImport(@Body body: Map<String, @JvmSuppressWildcards Any?>): TransactionImportPreview

    @POST("finance/budgets/transactions/categorize")
    suspend fun categorizeAllTransactions(@Query("referenceDate") referenceDate: String)

    @POST("finance/budgets/unbudgeted/categorize")
    suspend fun categorizeUnbudgeted(@Query("referenceDate") referenceDate: String)
} 