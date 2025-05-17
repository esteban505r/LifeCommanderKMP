package com.lifecommander.finance.model

import com.esteban.ruano.lifecommander.models.finance.Category
import com.lifecommander.models.Frequency
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
    val frequency: Frequency? = null
)

@Serializable
enum class TransactionType {
    INCOME,
    EXPENSE,
    TRANSFER
}


