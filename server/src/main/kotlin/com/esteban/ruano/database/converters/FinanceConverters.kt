package com.esteban.ruano.database.converters

import com.esteban.ruano.database.entities.Account
import com.esteban.ruano.database.entities.Transaction
import com.esteban.ruano.database.entities.Budget
import com.esteban.ruano.database.entities.SavingsGoal
import com.esteban.ruano.database.entities.ScheduledTransaction
import com.esteban.ruano.lifecommander.models.finance.Category.Companion.toCategory
import com.esteban.ruano.models.finance.*
import com.esteban.ruano.utils.DateUIUtils.formatDefault
import com.esteban.ruano.utils.DateUtils.parseDateTime
import com.lifecommander.models.Frequency
import com.lifecommander.finance.model.Transaction as TransactionDomainModel
import com.lifecommander.finance.model.TransactionType as TransactionTypeDomainModel
import com.esteban.ruano.lifecommander.models.finance.Category as CategoryDomainModel


fun Account.toResponseDTO(): AccountResponseDTO {
    return AccountResponseDTO(
        id = this.id.value.toString().toString(),
        name = this.name,
        type = this.type,
        initialBalance = this.initialBalance.toDouble(),
        currency = this.currency,
        balance = 0.toDouble(),
    )
}

fun Transaction.toDomainModel(): TransactionDomainModel {
    return TransactionDomainModel(
        id = this.id.value.toString().toString(),
        amount = this.amount.toDouble(),
        description = this.description,
        date = this.date.parseDateTime(),
        type = TransactionTypeDomainModel.valueOf(this.type.toString()),
        category = CategoryDomainModel.valueOf(this.category),
        accountId = this.account.id.value.toString().toString(),
    )
}

fun Transaction.toResponseDTO(): TransactionResponseDTO {
    return TransactionResponseDTO(
        id = this.id.value.toString(),
        amount = this.amount.toDouble(),
        description = this.description,
        date = this.date.parseDateTime(),
        type = this.type,
        category = this.category,
        accountId = this.account.id.value.toString(),
        status = this.status.toString(),
    )
}

fun Budget.toDomainModel(): com.esteban.ruano.lifecommander.models.finance.Budget {
    return com.esteban.ruano.lifecommander.models.finance.Budget(
        id = this.id.value.toString(),
        name = this.name,
        amount = this.amount.toDouble(),
        category = this.category.toCategory(),
        startDate = this.startDate.formatDefault(),
        endDate = this.endDate?.formatDefault(),
        frequency = Frequency.valueOf(this.frequency.toString()),
    )
}

fun Budget.toResponseDTO(): BudgetResponseDTO {
    return BudgetResponseDTO(
        id = this.id.value.toString(),
        name = this.name,
        amount = this.amount.toDouble(),
        category = this.category,
        startDate = this.startDate.formatDefault(),
        endDate = this.endDate?.formatDefault(),
        frequency = this.frequency.value,
    )
}

fun SavingsGoal.toResponseDTO(): SavingsGoalResponseDTO {
    return SavingsGoalResponseDTO(
        id = this.id.value.toString(),
        name = this.name,
        targetAmount = this.targetAmount.toDouble(),
        currentAmount = this.currentAmount.toDouble(),
        targetDate = this.targetDate.formatDefault()
    )
}

fun ScheduledTransaction.toResponseDTO(): ScheduledTransactionResponseDTO {
    return ScheduledTransactionResponseDTO(
        id = this.id.value.toString(),
        description = this.description,
        amount = this.amount.toDouble(),
        startDate = this.startDate.formatDefault(),
        frequency = this.frequency.toString(),
        interval = this.interval,
        type = this.type,
        category = this.category,
        accountId = this.account.id.value.toString(),
        applyAutomatically = this.applyAutomatically,
        status = this.status.toString()
    )
}

fun ScheduledTransaction.toDomainModel(): com.lifecommander.finance.model.ScheduledTransaction {
    return com.lifecommander.finance.model.ScheduledTransaction(
        id = this.id.value.toString(),
        description = this.description,
        amount = this.amount.toDouble(),
        frequency = this.frequency.value,
        interval = this.interval,
        type = TransactionTypeDomainModel.valueOf(this.type.toString()),
        category = CategoryDomainModel.valueOf(this.category),
        accountId = this.account.id.value.toString(),
        startDate = this.startDate.formatDefault(),
    )
} 