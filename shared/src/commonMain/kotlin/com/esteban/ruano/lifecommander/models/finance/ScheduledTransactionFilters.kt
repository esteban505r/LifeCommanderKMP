package com.esteban.ruano.lifecommander.models.finance

import com.lifecommander.models.Frequency

data class ScheduledTransactionFilters(
    val searchPattern: String? = null,
    val categories: List<String>? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val types: List<com.lifecommander.finance.model.TransactionType>? = null,
    val minAmount: Double? = null,
    val maxAmount: Double? = null,
    val accountIds: List<String>? = null,
    val frequencies: List<Frequency>? = null,
    val applyAutomatically: Boolean? = null,
    val amountSortOrder: SortOrder = SortOrder.NONE,
) 