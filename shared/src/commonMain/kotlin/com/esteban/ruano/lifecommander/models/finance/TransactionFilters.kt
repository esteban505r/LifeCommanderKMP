package com.esteban.ruano.lifecommander.models.finance

enum class SortOrder {
    NONE,
    ASCENDING,
    DESCENDING
}

data class TransactionFilters(
    val searchPattern: String? = null,
    val categories: List<String>? = null,
    val startDate: String? = null,
    val startDateHour: String? = null,
    val endDate: String? = null,
    val endDateHour: String? = null,
    val types: List<com.lifecommander.finance.model.TransactionType>? = null,
    val minAmount: Double? = null,
    val maxAmount: Double? = null,
    val accountIds: List<String>? = null,
    val amountSortOrder: SortOrder = SortOrder.NONE,
)
