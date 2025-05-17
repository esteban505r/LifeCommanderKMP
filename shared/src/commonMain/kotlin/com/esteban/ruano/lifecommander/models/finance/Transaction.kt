package com.lifecommander.finance.model

import com.esteban.ruano.lifecommander.models.finance.Category
import kotlinx.serialization.Serializable

@Serializable
data class Transaction(
    val id: String? = null,
    val amount: Double,
    val type: TransactionType,
    val category: Category,
    val accountId: String,
    val description: String,
    val date: String,
    val isRecurring: Boolean = false,
    val recurrence: Recurrence? = null
)

@Serializable
enum class TransactionType {
    INCOME,
    EXPENSE,
    TRANSFER
}



@Serializable
enum class Recurrence {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
} 