package com.esteban.ruano.lifecommander.models.finance

data class BudgetFilters(
    val searchPattern: String? = null,
    val categories: List<String>? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val minAmount: Double? = null,
    val maxAmount: Double? = null,
    val isOverBudget: Boolean? = null
) 