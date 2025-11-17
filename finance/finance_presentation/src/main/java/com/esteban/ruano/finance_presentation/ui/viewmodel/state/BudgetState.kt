package com.esteban.ruano.finance_presentation.ui.viewmodel.state

import com.esteban.ruano.core_ui.view_model.ViewState
import com.esteban.ruano.lifecommander.models.finance.BudgetFilters
import com.esteban.ruano.lifecommander.models.finance.BudgetProgress
import com.esteban.ruano.lifecommander.models.finance.TransactionFilters
import com.esteban.ruano.utils.DateUIUtils.formatDefault
import com.esteban.ruano.utils.DateUIUtils.getCurrentDateTime
import com.lifecommander.finance.model.Transaction
import kotlinx.datetime.TimeZone

data class BudgetState(
    val budgets: List<BudgetProgress> = emptyList(),
    val budgetFilters: BudgetFilters = BudgetFilters(),
    val budgetBaseDate: String = getCurrentDateTime(
        TimeZone.currentSystemDefault()
    ).date.formatDefault(),
    val budgetTransactions: List<Transaction> = emptyList(),
    val transactionFilters: TransactionFilters = TransactionFilters(),
    val isLoading: Boolean = false,
    val error: String? = null
) : ViewState

