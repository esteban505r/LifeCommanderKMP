package com.lifecommander.finance.model

import com.esteban.ruano.lifecommander.models.finance.Category
import com.lifecommander.models.Frequency
import kotlinx.serialization.Serializable

@Serializable
data class ScheduledTransaction(
    val id: String? = null,
    val amount: Double,
    val type: TransactionType,
    val category: Category,
    val accountId: String,
    val description: String,
    val startDate: String,
    val frequency: String? = null,
    val interval: Int? = null,
    val applyAutomatically: Boolean = false,
)



