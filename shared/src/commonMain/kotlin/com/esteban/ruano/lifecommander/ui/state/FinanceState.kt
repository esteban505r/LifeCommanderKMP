package com.esteban.ruano.lifecommander.ui.state

import com.esteban.ruano.lifecommander.models.finance.BudgetFilters
import com.esteban.ruano.lifecommander.models.finance.BudgetProgress
import com.esteban.ruano.lifecommander.models.finance.TransactionFilters
import com.esteban.ruano.utils.DateUIUtils.formatDefault
import com.esteban.ruano.utils.DateUIUtils.getCurrentDateTime
import com.lifecommander.finance.model.Account
import com.lifecommander.finance.model.SavingsGoal
import com.lifecommander.finance.model.ScheduledTransaction
import com.lifecommander.finance.model.Transaction
import com.lifecommander.finance.model.TransactionImportPreview
import kotlinx.datetime.TimeZone

data class FinanceState(
    val selectedTab: Int = 0,
    val accounts: List<Account> = emptyList(),
    val transactions: List<Transaction> = emptyList(),
    val totalTransactions: Long = 0,
    val scheduledTransactions: List<ScheduledTransaction> = emptyList(),
    val totalScheduledTransactions: Long = 0,
    val budgets: List<BudgetProgress> = emptyList(),
    val currentBudgetId: String? = null,
    val savingsGoals: List<SavingsGoal> = emptyList(),
    val savingsGoalProgress: Map<String, Double> = emptyMap(),
    val selectedAccount: Account? = null,
    // Tab-specific loading states
    val isLoadingAccounts: Boolean = false,
    val isLoadingTransactions: Boolean = false,
    val isLoadingScheduledTransactions: Boolean = false,
    val isLoadingBudgets: Boolean = false,
    val isLoadingSavingsGoals: Boolean = false,
    // Legacy global loading state (deprecated, kept for backward compatibility)
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 0,
    val pageSize: Int = 50,
    val transactionFilters: TransactionFilters = TransactionFilters(),
    val budgetFilters: BudgetFilters = BudgetFilters(),
    val budgetBaseDate: String = getCurrentDateTime(
        TimeZone.currentSystemDefault()
    ).date.formatDefault(),
    val importPreview: TransactionImportPreview? = null,
)