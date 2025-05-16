package com.lifecommander.finance.model

data class ImportTransactionsResponse(
    val transactionIds: List<String>
)

data class ImportTransactionsRequest(
    val text: String,
    val accountId: String
)
