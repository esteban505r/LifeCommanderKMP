package com.esteban.ruano.service

import com.esteban.ruano.database.converters.toResponseDTO
import com.esteban.ruano.database.entities.*
import com.esteban.ruano.database.models.Status
import com.esteban.ruano.database.models.TransactionType
import com.esteban.ruano.models.finance.*
import com.esteban.ruano.utils.DateUIUtils.toLocalDate
import com.esteban.ruano.utils.DateUIUtils.toLocalDateTime
import com.esteban.ruano.utils.TransactionParser
import com.lifecommander.finance.model.TransactionImportPreview
import com.lifecommander.finance.model.TransactionImportPreviewItem
import kotlinx.datetime.atTime
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class TransactionService : BaseService() {
    fun createTransaction(
        userId: Int,
        amount: Double,
        description: String,
        date: String,
        type: TransactionType,
        category: String,
        accountId: UUID
    ): UUID? {
        return transaction {
            Transactions.insertOperation(userId) {
                insert {
                    it[this.amount] = amount.toBigDecimal()
                    it[this.description] = description
                    it[this.date] = date.toLocalDateTime()
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

    fun getTransactionsByDateRange(userId: Int, startDate: String, endDate: String): List<TransactionResponseDTO> {
        return transaction {
            Transaction.find { 
                (Transactions.user eq userId) and 
                (Transactions.date greaterEq startDate.toLocalDateTime()) and
                (Transactions.date lessEq endDate.toLocalDateTime())
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
        date: String? = null,
        type: TransactionType? = null,
        category: String? = null
    ): Boolean {
        return transaction {
            val transaction = Transaction.findById(transactionId)
            if (transaction != null && transaction.user.id.value == userId) {
                amount?.let { transaction.amount = it.toBigDecimal() }
                description?.let { transaction.description = it }
                date?.let { transaction.date = it.toLocalDateTime() }
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

    fun importTransactions(
        userId: Int,
        transactions: List<com.lifecommander.finance.model.Transaction>,
        skipDuplicates: Boolean = true
    ): List<UUID> {
        return transaction {
            val existingTransactions = Transaction.find { 
                (Transactions.user eq userId) and 
                (Transactions.account eq UUID.fromString(transactions.firstOrNull()?.accountId ?: "")) and
                (Transactions.status eq Status.ACTIVE)
            }.map { it.toResponseDTO() }

            transactions
                .filter { transaction ->
                    if (skipDuplicates) {
                        !existingTransactions.any { existing ->
                            existing.amount == transaction.amount &&
                            existing.description == transaction.description &&
                            existing.date == transaction.date &&
                            existing.type.toString() == transaction.type.toString()
                        }
                    } else {
                        true
                    }
                }
                .mapNotNull { transaction ->
                    Transactions.insertOperation(userId) {
                        insert {
                            it[this.amount] = transaction.amount.toBigDecimal()
                            it[this.description] = transaction.description
                            it[this.date] = transaction.date.toLocalDate().atTime(0, 0)
                            it[this.type] = TransactionType.valueOf(transaction.type.toString())
                            it[this.category] = transaction.category.toString()
                            it[this.account] = UUID.fromString(transaction.accountId)
                            it[this.user] = userId
                        }.resultedValues?.firstOrNull()?.getOrNull(this.id)?.value
                    }
                }
        }
    }

    fun importTransactionsFromText(
        userId: Int,
        text: String,
        accountId: String,
        skipDuplicates: Boolean = true
    ): List<UUID> {
        val transactions = TransactionParser.parseTransactions(text, accountId)
        return importTransactions(userId, transactions, skipDuplicates)
    }

    fun previewTransactionImport(
        userId: Int,
        text: String,
        accountId: String
    ): TransactionImportPreview {
        val parsedTransactions = TransactionParser.parseTransactions(text, accountId)
        return transaction {
            val existingTransactions = Transaction.find { 
                (Transactions.user eq userId) and 
                (Transactions.account eq UUID.fromString(accountId)) and
                (Transactions.status eq Status.ACTIVE)
            }.map { it.toResponseDTO() }

            val previewItems = parsedTransactions.map { transaction ->
                val isDuplicate = existingTransactions.any { existing ->
                    existing.amount == transaction.amount &&
                    existing.description == transaction.description &&
                    existing.date == transaction.date &&
                    existing.type.toString() == transaction.type.toString()
                }

                TransactionImportPreviewItem(
                    transaction = transaction,
                    isDuplicate = isDuplicate
                )
            }

            TransactionImportPreview(
                items = previewItems,
                totalTransactions = previewItems.size,
                duplicateCount = previewItems.count { it.isDuplicate },
                totalAmount = previewItems.sumOf { it.transaction.amount }
            )
        }
    }
}


