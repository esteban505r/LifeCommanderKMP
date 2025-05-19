package com.lifecommander.finance.model

import kotlinx.serialization.Serializable

@Serializable
data class ImportTransactionsResponse(
    val transactionIds: List<String>
)

@Serializable
data class ImportTransactionsRequest(
    val text: String,
    val accountId: String
)
