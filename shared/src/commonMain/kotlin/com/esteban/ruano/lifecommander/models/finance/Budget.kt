package com.lifecommander.finance.model

import com.esteban.ruano.lifecommander.models.finance.Category
import com.lifecommander.models.Frequency
import kotlinx.serialization.Serializable

@Serializable
data class Budget(
    val id: String? = null,
    val name: String,
    val amount: Double,
    val category: Category,
    val startDate: String,
    val frequency: Frequency,
    val isRecurring: Boolean = false,
    val recurrence: Recurrence? = null,
    val rollover: Boolean = false,
    val rolloverAmount: Double = 0.0
)

@Serializable
data class BudgetProgress(
    val budget: Budget,
    val spent: Double = 0.0,
    val remaining: Double = 0.0,
    val percentageUsed: Double = 0.0,
    val isOverBudget: Boolean = false
) 