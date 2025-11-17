package com.esteban.ruano.finance_presentation.ui.viewmodel.state

import com.esteban.ruano.core_ui.view_model.ViewState
import com.esteban.ruano.lifecommander.models.finance.TransactionFilters
import com.lifecommander.finance.model.Transaction
import com.lifecommander.finance.model.TransactionImportPreview

data class TransactionState(
    val transactions: List<Transaction> = emptyList(),
    val totalTransactions: Long = 0,
    val transactionFilters: TransactionFilters = TransactionFilters(),
    val currentPage: Int = 0,
    val pageSize: Int = 20,
    val isLoading: Boolean = false,
    val isLoadingTransactions: Boolean = false,
    val error: String? = null,
    val importPreview: TransactionImportPreview? = null
) : ViewState

