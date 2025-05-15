package com.esteban.ruano.database.converters

import com.esteban.ruano.database.entities.Account
import com.esteban.ruano.database.entities.Transaction
import com.esteban.ruano.database.entities.Budget
import com.esteban.ruano.database.entities.SavingsGoal
import com.esteban.ruano.models.finance.*

fun Account.toResponseDTO(): AccountResponseDTO {
    return AccountResponseDTO(
        id = this.id.value,
        name = this.name,
        type = this.type,
        balance = this.balance.toDouble(),
        currency = this.currency
    )
}

fun Transaction.toResponseDTO(): TransactionResponseDTO {
    return TransactionResponseDTO(
        id = this.id.value,
        amount = this.amount.toDouble(),
        description = this.description,
        date = this.date,
        type = this.type,
        category = this.category,
        accountId = this.account.id.value,
    )
}

fun Budget.toResponseDTO(): BudgetResponseDTO {
    return BudgetResponseDTO(
        id = this.id.value,
        name = this.name,
        amount = this.amount.toDouble(),
        category = this.category,
        startDate = this.startDate,
        endDate = this.endDate
    )
}

fun SavingsGoal.toResponseDTO(): SavingsGoalResponseDTO {
    return SavingsGoalResponseDTO(
        id = this.id.value,
        name = this.name,
        targetAmount = this.targetAmount.toDouble(),
        currentAmount = this.currentAmount.toDouble(),
        targetDate = this.targetDate
    )
} 