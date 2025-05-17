package com.esteban.ruano.models.finance

import com.esteban.ruano.database.models.AccountType
import com.esteban.ruano.database.models.TransactionType
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import java.util.UUID

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
    val id: UUID,
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
data class TransactionResponseDTO(
    val id: UUID,
    val amount: Double,
    val description: String,
    val date: String,
    val type: TransactionType,
    val category: String,
    val accountId: UUID,
    val isRecurring: Boolean = false,
    val recurrence: String? = null,
    val status: String = "ACTIVE"
)

@Serializable
data class CreateBudgetDTO(
    val name: String,
    val amount: Double,
    val category: String,
    val startDate: LocalDate,
    val endDate: LocalDate
)

@Serializable
data class UpdateBudgetDTO(
    val name: String? = null,
    val amount: Double? = null,
    val category: String? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null
)

@Serializable
data class BudgetResponseDTO(
    val id: UUID,
    val name: String,
    val amount: Double,
    val category: String,
    val startDate: LocalDate,
    val endDate: LocalDate
)

@Serializable
data class CreateSavingsGoalDTO(
    val name: String,
    val targetAmount: Double,
    val targetDate: LocalDate
)

@Serializable
data class UpdateSavingsGoalDTO(
    val name: String? = null,
    val targetAmount: Double? = null,
    val targetDate: LocalDate? = null
)

@Serializable
data class SavingsGoalResponseDTO(
    val id: UUID,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val targetDate: LocalDate
)

@Serializable
data class UpdateSavingsGoalProgressDTO(
    val amount: Double
)

@Serializable
data class DateRangeDTO(
    val startDate: LocalDateTime,
    val endDate: LocalDateTime
)

@Serializable
data class DateRangeQueryDTO(
    val startDate: String,
    val endDate: String
)

@Serializable
data class ProgressResponseDTO(
    val progress: Double
)

@Serializable
data class RemainingAmountResponseDTO(
    val remaining: Double
)

@Serializable
data class TotalBalanceResponseDTO(
    val totalBalance: Double
) 