package com.esteban.ruano.lifecommander.models.finance

import com.lifecommander.finance.model.Transaction

data class TransactionsResponse(
    val transactions: List<Transaction>,
    val totalCount: Long
)
