package com.lifecommander.finance.model

import kotlinx.serialization.Serializable

@Serializable
data class TransactionImportPreview(
    val items: List<TransactionImportPreviewItem>,
    val totalTransactions: Int,
    val duplicateCount: Int,
    val totalAmount: Double
)

@Serializable
data class TransactionImportPreviewItem(
    val transaction: Transaction,
    val isDuplicate: Boolean
)

@Serializable
data class TransactionImportPreviewRequest(
    val text: String,
    val accountId: String
)