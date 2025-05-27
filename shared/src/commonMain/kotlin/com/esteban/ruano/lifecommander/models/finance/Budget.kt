package com.esteban.ruano.lifecommander.models.finance

import com.lifecommander.models.Frequency
import kotlinx.serialization.Serializable

@Serializable
data class Budget(
    val id: String? = null,
    val name: String,
    val amount: Double,
    val category: Category,
    val startDate: String,
    val endDate: String? = null,
    val frequency: Frequency? = null,
    val rollover: Boolean = false,
    val rolloverAmount: Double = 0.0
)

@Serializable
data class BudgetProgress(
    val budget: Budget,
    val spent: Double = 0.0
){
    val remaining: Double
        get() = budget.amount - spent

    val progressPercentage: Double
        get() = if (budget.amount > 0) (spent / budget.amount) * 100 else 0.0

    val isOverBudget: Boolean
        get() = spent > budget.amount
}