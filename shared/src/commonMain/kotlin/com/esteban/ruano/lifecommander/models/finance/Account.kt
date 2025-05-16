package com.lifecommander.finance.model

import kotlinx.serialization.Serializable

@Serializable
data class Account(
    val id: String? = null,
    val name: String,
    val type: AccountType,
    val balance: Double,
    val currency: String = "USD",
    val isArchived: Boolean = false
)

@Serializable
enum class AccountType {
    CHECKING,
    SAVINGS,
    CREDIT_CARD,
    INVESTMENT,
    CASH,
    LOAN,
    OTHER
} 