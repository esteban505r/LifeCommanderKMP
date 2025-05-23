package com.esteban.ruano.repository

import com.esteban.ruano.lifecommander.models.finance.ScheduledTransactionFilters
import com.esteban.ruano.models.finance.ScheduledTransactionResponseDTO
import com.esteban.ruano.models.finance.ScheduledTransactionsResponseDTO
import com.esteban.ruano.service.ScheduledTransactionService
import com.lifecommander.finance.model.TransactionType
import com.lifecommander.models.Frequency
import java.util.*

class ScheduledTransactionRepository(private val service: ScheduledTransactionService) {
    fun create(
        userId: Int,
        description: String,
        amount: Double,
        startDate: String,
        frequency: Frequency,
        interval: Int,
        type: TransactionType,
        category: String,
        accountId: UUID,
        applyAutomatically: Boolean
    ): UUID? = service.createScheduledTransaction(
        userId = userId,
        description = description,
        amount = amount,
        startDate = startDate,
        frequency = frequency,
        interval = interval,
        type = type,
        category = category,
        accountId = accountId,
        applyAutomatically = applyAutomatically
    )

    fun getAll(
        userId: Int,
        limit: Int = 50,
        offset: Int = 0,
        filters: ScheduledTransactionFilters = ScheduledTransactionFilters()
    ): ScheduledTransactionsResponseDTO = 
        service.getScheduledTransactionsByUser(
            userId = userId,
            limit = limit,
            offset = offset,
            filters = filters
        )

    fun getById(id: UUID, userId: Int): ScheduledTransactionResponseDTO? =
        service.getScheduledTransaction(id, userId)

    fun update(
        id: UUID,
        userId: Int,
        description: String? = null,
        amount: Double? = null,
        startDate: String? = null,
        frequency: Frequency? = null,
        interval: Int? = null,
        type: TransactionType? = null,
        category: String? = null,
        applyAutomatically: Boolean? = null
    ): Boolean = service.updateScheduledTransaction(
        id = id,
        userId = userId,
        description = description,
        amount = amount,
        startDate = startDate,
        frequency = frequency,
        interval = interval,
        type = type,
        category = category,
        applyAutomatically = applyAutomatically
    )

    fun delete(userId: Int, transactionId: UUID): Boolean =
        service.deleteScheduledTransaction(transactionId, userId)

    fun getByAccount(accountId: UUID): List<ScheduledTransactionResponseDTO> =
        service.getScheduledTransactionsByAccount(accountId)
} 