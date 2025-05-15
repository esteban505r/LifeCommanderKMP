package com.esteban.ruano.service

import com.esteban.ruano.database.converters.toResponseDTO
import com.esteban.ruano.database.entities.*
import com.esteban.ruano.database.models.Status
import com.esteban.ruano.database.models.TransactionType
import com.esteban.ruano.models.finance.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class TransactionService : BaseService() {
    fun createTransaction(
        userId: Int,
        amount: Double,
        description: String,
        date: LocalDateTime,
        type: TransactionType,
        category: String,
        accountId: UUID
    ): UUID? {
        return transaction {
            Transactions.insertOperation(userId) {
                insert {
                    it[this.amount] = amount.toBigDecimal()
                    it[this.description] = description
                    it[this.date] = date
                    it[this.type] = type
                    it[this.category] = category
                    it[this.account] = accountId
                    it[this.user] = userId
                }.resultedValues?.firstOrNull()?.getOrNull(this.id)?.value
            }
        }
    }

    fun getTransactionsByUser(userId: Int): List<TransactionResponseDTO> {
        return transaction {
            Transaction.find { Transactions.user eq userId }
                .map { it.toResponseDTO() }
        }
    }

    fun getTransactionsByAccount(accountId: UUID): List<TransactionResponseDTO> {
        return transaction {
            Transaction.find { Transactions.account eq accountId }
                .map { it.toResponseDTO() }
        }
    }

    fun getTransactionsByDateRange(userId: Int, startDate: LocalDateTime, endDate: LocalDateTime): List<TransactionResponseDTO> {
        return transaction {
            Transaction.find { 
                (Transactions.user eq userId) and 
                (Transactions.date greaterEq startDate) and 
                (Transactions.date lessEq endDate)
            }.map { it.toResponseDTO() }
        }
    }

    fun getTransaction(transactionId: UUID, userId: Int): TransactionResponseDTO? {
        return transaction {
            Transaction.find { (Transactions.id eq transactionId) and (Transactions.user eq userId) }
                .firstOrNull()
                ?.toResponseDTO()
        }
    }

    fun updateTransaction(
        transactionId: UUID,
        userId: Int,
        amount: Double? = null,
        description: String? = null,
        date: LocalDateTime? = null,
        type: TransactionType? = null,
        category: String? = null
    ): Boolean {
        return transaction {
            val transaction = Transaction.findById(transactionId)
            if (transaction != null && transaction.user.id.value == userId) {
                amount?.let { transaction.amount = it.toBigDecimal() }
                description?.let { transaction.description = it }
                date?.let { transaction.date = it }
                type?.let { transaction.type = it }
                category?.let { transaction.category = it }
                true
            } else {
                false
            }
        }
    }

    fun deleteTransaction(transactionId: UUID, userId: Int): Boolean {
        return transaction {
            val transaction = Transaction.findById(transactionId)
            if (transaction != null && transaction.user.id.value == userId) {
                transaction.status = Status.INACTIVE
                true
            } else {
                false
            }
        }
    }
} 