package com.esteban.ruano.finance_presentation.ui.viewmodel.state

import com.esteban.ruano.core_ui.view_model.ViewState
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

data class FinanceState(
    val selectedTab: FinanceTab = FinanceTab.OVERVIEW,
    val isLoading: Boolean = false,
    val error: String? = null,
    
    // Pagination
    val currentPage: Int = 0,
    val pageSize: Int = 20,
    
    // Accounts
    val accounts: List<Account> = emptyList(),
    val selectedAccount: Account? = null,
    
    // Transactions
    val transactions: List<Transaction> = emptyList(),
    val totalTransactions: Long = 0,
    val transactionFilters: TransactionFilters = TransactionFilters(),
    
    // Budgets
    val budgets: List<BudgetProgress> = emptyList(),
    val budgetFilters: BudgetFilters = BudgetFilters(),
    val budgetBaseDate: String = getCurrentDateTime().date.formatDefault(),
    
    // Savings Goals
    val savingsGoals: List<SavingsGoal> = emptyList(),
    val savingsGoalProgress: Map<String, Double> = emptyMap(),
    
    // Scheduled Transactions
    val scheduledTransactions: List<ScheduledTransaction> = emptyList(),
    val totalScheduledTransactions: Long = 0,
    
    // Import
    val importPreview: TransactionImportPreview? = null
) : ViewState