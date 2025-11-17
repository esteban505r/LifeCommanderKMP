package com.esteban.ruano.finance_presentation.ui.viewmodel.state

import com.esteban.ruano.core_ui.view_model.ViewState
import com.esteban.ruano.lifecommander.models.finance.TransactionFilters
import com.lifecommander.finance.model.ScheduledTransaction

data class ScheduledTransactionState(
    val scheduledTransactions: List<ScheduledTransaction> = emptyList(),
    val totalScheduledTransactions: Long = 0,
    val transactionFilters: TransactionFilters = TransactionFilters(),
    val currentPage: Int = 0,
    val pageSize: Int = 20,
    val isLoading: Boolean = false,
    val error: String? = null
) : ViewState

