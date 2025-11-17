package com.esteban.ruano.finance_presentation.ui.intent

import com.esteban.ruano.core_ui.view_model.UserIntent
import com.esteban.ruano.lifecommander.models.finance.TransactionFilters
import com.lifecommander.finance.model.Transaction

sealed class TransactionIntent : UserIntent {
    data class GetTransactions(val refresh: Boolean = false) : TransactionIntent()
    data class AddTransaction(val transaction: Transaction) : TransactionIntent()
    data class UpdateTransaction(val transaction: Transaction) : TransactionIntent()
    data class DeleteTransaction(val id: String) : TransactionIntent()
    data class ChangeTransactionFilters(val filters: TransactionFilters) : TransactionIntent()
    data class ImportTransactions(
        val text: String,
        val accountId: String,
        val skipDuplicates: Boolean
    ) : TransactionIntent()
    data class PreviewTransactionImport(
        val text: String,
        val accountId: String
    ) : TransactionIntent()
}

