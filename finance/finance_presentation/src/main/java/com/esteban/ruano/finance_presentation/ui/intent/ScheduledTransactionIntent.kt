package com.esteban.ruano.finance_presentation.ui.intent

import com.esteban.ruano.core_ui.view_model.UserIntent
import com.esteban.ruano.lifecommander.models.finance.TransactionFilters
import com.lifecommander.finance.model.ScheduledTransaction

sealed class ScheduledTransactionIntent : UserIntent {
    data class GetScheduledTransactions(val refresh: Boolean = false) : ScheduledTransactionIntent()
    data class AddScheduledTransaction(val transaction: ScheduledTransaction) : ScheduledTransactionIntent()
    data class UpdateScheduledTransaction(val transaction: ScheduledTransaction) : ScheduledTransactionIntent()
    data class DeleteScheduledTransaction(val id: String) : ScheduledTransactionIntent()
    data class ChangeTransactionFilters(val filters: TransactionFilters) : ScheduledTransactionIntent()
}

