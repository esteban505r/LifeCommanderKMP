package com.esteban.ruano.database.converters

import com.esteban.ruano.database.entities.Account
import com.esteban.ruano.database.entities.Transaction
import com.esteban.ruano.database.entities.Budget
import com.esteban.ruano.database.entities.SavingsGoal
import com.esteban.ruano.models.finance.*
import com.esteban.ruano.utils.DateUtils.parseDateTime
import com.lifecommander.finance.model.Transaction as TransactionDomainModel
import com.lifecommander.finance.model.TransactionType as TransactionTypeDomainModel
import com.lifecommander.finance.model.Category as CategoryDomainModel


fun Account.toResponseDTO(): AccountResponseDTO {
    return AccountResponseDTO(
        id = this.id.value,
        name = this.name,
        type = this.type,
        initialBalance = this.initialBalance.toDouble(),
        currency = this.currency,
        balance = 0.toDouble(),
    )
}

fun Transaction.toDomainModel(): TransactionDomainModel {
    return TransactionDomainModel(
        id = this.id.value.toString(),
        amount = this.amount.toDouble(),
        description = this.description,
        date = this.date.parseDateTime(),
        type = TransactionTypeDomainModel.valueOf(this.type.toString()),
        category = CategoryDomainModel.valueOf(this.category),
        accountId = this.account.id.value.toString(),
    )
}

fun Transaction.toResponseDTO(): TransactionResponseDTO {
    return TransactionResponseDTO(
        id = this.id.value,
        amount = this.amount.toDouble(),
        description = this.description,
        date = this.date.parseDateTime(),
        type = this.type,
        category = this.category,
        accountId = this.account.id.value,
        status = this.status.toString(),
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