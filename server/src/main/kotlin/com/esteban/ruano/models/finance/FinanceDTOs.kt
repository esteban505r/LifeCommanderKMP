package com.esteban.ruano.models.finance

import com.esteban.ruano.database.models.AccountType
import com.lifecommander.finance.model.TransactionType
import kotlinx.serialization.Serializable

@Serializable
data class CreateAccountDTO(
    val name: String,
    val type: AccountType,
    val balance: Double,
    val currency: String = "USD"
)

@Serializable
data class UpdateAccountDTO(
    val name: String? = null,
    val type: AccountType? = null,
    val initialBalance : Double? = null,
    val currency: String? = null
)

@Serializable
data class AccountResponseDTO(
    val id: String,
    val name: String,
    val type: AccountType,
    val balance: Double,
    val initialBalance: Double,
    val currency: String,
)

@Serializable
data class CreateTransactionDTO(
    val amount: Double,
    val description: String,
    val date: String,
    val type: TransactionType,
    val category: String,
    val accountId: String
)

@Serializable
data class UpdateTransactionDTO(
    val amount: Double? = null,
    val description: String? = null,
    val date: String? = null,
    val type: TransactionType? = null,
    val category: String? = null
)
@Serializable
data class TransactionsResponseDTO(
    val transactions: List<TransactionResponseDTO>,
    val totalCount: Long
)

@Serializable
data class TransactionResponseDTO(
    val id: String,
    val amount: Double,
    val description: String,
    val date: String,
    val type: TransactionType,
    val category: String,
    val accountId: String,
    val isRecurring: Boolean = false,
    val recurrence: String? = null,
    val status: String = "ACTIVE"
)

@Serializable
data class CreateBudgetDTO(
    val name: String,
    val amount: Double,
    val category: String,
    val startDate: String,
    val endDate: String? = null,
)

@Serializable
data class UpdateBudgetDTO(
    val name: String? = null,
    val amount: Double? = null,
    val category: String? = null,
    val startDate: String? = null,
    val endDate: String? = null
)

@Serializable
data class BudgetResponseDTO(
    val id: String,
    val name: String,
    val amount: Double,
    val category: String,
    val frequency: String,
    val startDate: String,
    val endDate: String? = null,
)

@Serializable
data class CreateSavingsGoalDTO(
    val name: String,
    val targetAmount: Double,
    val targetDate: String
)

@Serializable
data class UpdateSavingsGoalDTO(
    val name: String? = null,
    val targetAmount: Double? = null,
    val targetDate: String? = null
)

@Serializable
data class SavingsGoalResponseDTO(
    val id: String,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val targetDate: String
)

@Serializable
data class UpdateSavingsGoalProgressDTO(
    val amount: Double
)

@Serializable
data class DateRangeDTO(
    val startDate: String,
    val endDate: String
)

@Serializable
data class DateRangeQueryDTO(
    val startDate: String,
    val endDate: String
)

@Serializable
data class BudgetProgressResponseDTO(
    val budget: BudgetResponseDTO,
    val spent: Double = 0.0,
)

@Serializable
data class RemainingAmountResponseDTO(
    val remaining: Double
)

@Serializable
data class TotalBalanceResponseDTO(
    val totalBalance: Double
)