package com.esteban.ruano.lifecommander.utils

import com.esteban.ruano.lifecommander.models.finance.TransactionFilters


fun TransactionFilters.buildParametersString() : String?{
    val params = mutableListOf<String>()
    val filters = this
    filters.searchPattern?.let { params.add("search=$it") }
    filters.categories?.forEach { params.add("category=$it") }
    filters.startDate?.let { params.add("startDate=$it") }
    filters.startDateHour?.let { params.add("startDateHour=$it") }
    filters.endDate?.let { params.add("endDate=$it") }
    filters.endDateHour?.let { params.add("endDateHour=$it") }
    filters.types?.forEach { params.add("type=$it") }
    filters.amountSortOrder?.let { params.add("amountSortOrder=$it") }
    filters.minAmount?.let { params.add("minAmount=$it") }
    filters.maxAmount?.let { params.add("maxAmount=$it") }
    filters.accountIds?.forEach { params.add("accountId=$it") }
    if (params.isNotEmpty()) {
        return params.joinToString("&")
    }
    return null
}