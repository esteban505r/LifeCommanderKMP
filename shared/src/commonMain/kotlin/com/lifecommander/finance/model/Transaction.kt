package com.lifecommander.finance.model

import kotlinx.serialization.Serializable
import kotlinx.datetime.LocalDateTime

@Serializable
data class Transaction(
    val id: String? = null,
    val amount: Double,
    val type: TransactionType,
    val category: Category,
    val accountId: String,
    val description: String,
    val date: LocalDateTime,
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
enum class Category {
    // Income categories
    SALARY,
    INVESTMENT,
    FREELANCE,
    GIFT,
    
    // Expense categories
    FOOD,
    TRANSPORTATION,
    HOUSING,
    UTILITIES,
    ENTERTAINMENT,
    SHOPPING,
    HEALTH,
    EDUCATION,
    TRAVEL,
    OTHER
}

@Serializable
enum class Recurrence {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
} 