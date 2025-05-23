package com.esteban.ruano.lifecommander.models.finance

import com.lifecommander.finance.model.ScheduledTransaction

data class ScheduledTransactionsResponse(
    val transactions: List<ScheduledTransaction>,
    val totalCount: Long
)
