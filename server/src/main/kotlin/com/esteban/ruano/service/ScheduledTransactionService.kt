package com.esteban.ruano.service

import com.esteban.ruano.database.converters.toDomainModel
import com.esteban.ruano.database.converters.toResponseDTO
import com.esteban.ruano.database.entities.*
import com.esteban.ruano.database.models.Status
import com.esteban.ruano.lifecommander.models.finance.ScheduledTransactionFilters
import com.esteban.ruano.models.finance.*
import com.esteban.ruano.utils.DateUIUtils.formatDefault
import com.esteban.ruano.utils.DateUIUtils.toLocalDate
import com.esteban.ruano.utils.DateUIUtils.toLocalDateTime
import com.esteban.ruano.utils.absAmount
import com.esteban.ruano.utils.addSortOrder
import com.esteban.ruano.utils.buildScheduledTransactionFilters
import com.lifecommander.finance.model.TransactionType
import com.lifecommander.models.Frequency
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class ScheduledTransactionService : BaseService() {
    fun createScheduledTransaction(
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
    ): UUID? {
        return transaction {
            ScheduledTransactions.insertOperation(userId) {
                insert {
                    it[this.description] = description
                    it[this.amount] = amount.toBigDecimal()
                    it[this.startDate] = startDate.toLocalDateTime()
                    it[this.frequency] = frequency
                    it[this.interval] = interval
                    it[this.type] = type
                    it[this.category] = category
                    it[this.account] = accountId
                    it[this.applyAutomatically] = applyAutomatically
                    it[this.user] = userId
                }.resultedValues?.firstOrNull()?.getOrNull(this.id)?.value
            }
        }
    }

    fun getScheduledTransactionsByUser(
        userId: Int,
        limit: Int = 50,
        offset: Int = 0,
        filters: ScheduledTransactionFilters = ScheduledTransactionFilters()
    ): ScheduledTransactionsResponseDTO {
        return transaction {
            val baseQuery = ScheduledTransactions.selectAll().where {
                buildScheduledTransactionFilters(
                    userId = userId,
                    filters = filters,
                )
            }

            val totalCount = baseQuery.count()

            val paginatedResults = baseQuery
                .addSortOrder(
                    filters.amountSortOrder,
                    absAmount,
                    defaultSortOrder = ScheduledTransactions.startDate to SortOrder.DESC
                )
                .limit(limit, offset.toLong())

            val results = paginatedResults.map {
                ScheduledTransactionResponseDTO(
                    id = it[ScheduledTransactions.id].value.toString(),
                    amount = it[ScheduledTransactions.amount].toDouble(),
                    description = it[ScheduledTransactions.description],
                    startDate = it[ScheduledTransactions.startDate].formatDefault(),
                    frequency = it[ScheduledTransactions.frequency].value,
                    interval = it[ScheduledTransactions.interval],
                    type = it[ScheduledTransactions.type],
                    category = it[ScheduledTransactions.category],
                    accountId = it[ScheduledTransactions.account].value.toString(),
                    applyAutomatically = it[ScheduledTransactions.applyAutomatically],
                    status = it[ScheduledTransactions.status].toString()
                )
            }

            ScheduledTransactionsResponseDTO(
                transactions = results.toList(),
                totalCount = totalCount
            )
        }
    }


    fun getScheduledTransaction(id: UUID, userId: Int): ScheduledTransactionResponseDTO? {
        return transaction {
            ScheduledTransaction.find {
                (ScheduledTransactions.id eq id) and
                (ScheduledTransactions.user eq userId) and
                (ScheduledTransactions.status eq Status.ACTIVE)
            }.firstOrNull()?.toResponseDTO()
        }
    }

    fun updateScheduledTransaction(
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
    ): Boolean {
        return transaction {
            val scheduledTransaction = ScheduledTransaction.findById(id)
            if (scheduledTransaction != null && scheduledTransaction.user.id.value == userId) {
                description?.let { scheduledTransaction.description = it }
                amount?.let { scheduledTransaction.amount = it.toBigDecimal() }
                startDate?.let { scheduledTransaction.startDate = it.toLocalDateTime() }
                frequency?.let { scheduledTransaction.frequency = it }
                interval?.let { scheduledTransaction.interval = it }
                type?.let { scheduledTransaction.type = it }
                category?.let { scheduledTransaction.category = it }
                applyAutomatically?.let { scheduledTransaction.applyAutomatically = it }
                true
            } else {
                false
            }
        }
    }

    fun deleteScheduledTransaction(id: UUID, userId: Int): Boolean {
        return transaction {
            val scheduledTransaction = ScheduledTransaction.findById(id)
            if (scheduledTransaction != null && scheduledTransaction.user.id.value == userId) {
                scheduledTransaction.status = Status.DELETED
                true
            } else {
                false
            }
        }
    }

    fun getScheduledTransactionsByAccount(accountId: UUID): List<ScheduledTransactionResponseDTO> {
        return transaction {
            ScheduledTransaction.find { 
                (ScheduledTransactions.account eq accountId) and 
                (ScheduledTransactions.status eq Status.ACTIVE)
            }.map { it.toResponseDTO() }
        }
    }
} 