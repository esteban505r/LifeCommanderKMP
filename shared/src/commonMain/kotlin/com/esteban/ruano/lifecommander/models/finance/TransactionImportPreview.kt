package com.lifecommander.finance.model

data class TransactionImportPreview(
    val items: List<TransactionImportPreviewItem>,
    val totalTransactions: Int,
    val duplicateCount: Int,
    val totalAmount: Double
)

data class TransactionImportPreviewItem(
    val transaction: Transaction,
    val isDuplicate: Boolean
)

data class TransactionImportPreviewRequest(
    val text: String,
    val accountId: String
)