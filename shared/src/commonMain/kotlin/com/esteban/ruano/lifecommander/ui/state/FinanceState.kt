package com.lifecommander.finance.model

import com.esteban.ruano.lifecommander.models.finance.BudgetProgress
import com.esteban.ruano.lifecommander.models.finance.TransactionFilters
import kotlinx.serialization.Serializable

data class FinanceState(
    val accounts: List<Account> = emptyList(),
    val selectedAccount: Account? = null,
    val transactions: List<Transaction> = emptyList(),
    val totalTransactions: Long = 0,
    val budgets: List<BudgetProgress> = emptyList(),
    val savingsGoals: List<SavingsGoal> = emptyList(),
    val savingsGoalProgress: Map<String, SavingsGoalProgress> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val importPreview: TransactionImportPreview? = null,
    val currentPage: Int = 0,
    val pageSize: Int = 50,
    val transactionFilters: TransactionFilters = TransactionFilters()
)