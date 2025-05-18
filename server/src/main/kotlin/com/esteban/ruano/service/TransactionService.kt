package com.esteban.ruano.service

import com.esteban.ruano.database.converters.toDomainModel
import com.esteban.ruano.database.converters.toResponseDTO
import com.esteban.ruano.database.entities.*
import com.esteban.ruano.database.models.Status
import com.esteban.ruano.lifecommander.models.finance.TransactionFilters
import com.esteban.ruano.models.finance.*
import com.esteban.ruano.utils.DateUIUtils.formatDefault
import com.esteban.ruano.utils.DateUIUtils.toLocalDateTime
import com.esteban.ruano.utils.DateUtils.toLocalTime
import com.esteban.ruano.utils.TransactionParser
import com.lifecommander.finance.model.TransactionImportPreview
import com.lifecommander.finance.model.TransactionImportPreviewItem
import com.lifecommander.finance.model.TransactionType
import kotlinx.datetime.LocalTime
import kotlinx.datetime.atTime
import kotlinx.datetime.toLocalDate
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
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


    fun getTransactionsByUser(
        userId: Int,
        limit: Int = 50,
        offset: Int = 0,
        filters: TransactionFilters = TransactionFilters()
    ): TransactionsResponseDTO {
        return transaction {
            val baseQuery = Transactions.selectAll().where { Transactions.user eq userId }

            val conditions = mutableListOf<Op<Boolean>>()

            filters.searchPattern?.let { pattern ->
                conditions += Transactions.description like "%$pattern%"
            }
            filters.categories?.let { categories ->
                conditions += Transactions.category inList categories
            }
            filters.startDate?.let { start ->
                val startDateTime = start.toLocalDate().atTime(filters.startDateHour?.toLocalTime()?: LocalTime(0,0))
                conditions += Transactions.date greaterEq startDateTime
            }
            filters.endDate?.let { end ->
                val endDateTime = end.toLocalDate().atTime(filters.endDateHour?.toLocalTime()?: LocalTime(0,0))
                conditions += Transactions.date lessEq endDateTime
            }
            filters.types?.let { types ->
                conditions += Transactions.type inList types
            }
            filters.minAmount?.let { min ->
                conditions += Transactions.amount greaterEq min.toBigDecimal()
            }
            filters.maxAmount?.let { max ->
                conditions += Transactions.amount lessEq max.toBigDecimal()
            }
            filters.accountIds?.let { accountIds ->
                conditions += Transactions.account inList accountIds.map { UUID.fromString(it) }
            }

// Combine with AND, safely handle 0 conditions
            val combined: Op<Boolean> = conditions.fold(Op.TRUE as Op<Boolean>) { acc, op -> acc and op }

// Apply to query
            val filteredQuery = baseQuery.andWhere { combined }

            // Get total count before pagination
            val totalCount = filteredQuery.count()

            // Apply pagination
            val paginatedResults = filteredQuery
                .orderBy(Transactions.date to SortOrder.DESC)
                .limit(limit, offset.toLong())

            val results = paginatedResults.map {
                TransactionResponseDTO(
                    id = it[Transactions.id].value.toString(),
                    amount = it[Transactions.amount].toDouble(),
                    description = it[Transactions.description],
                    date = it[Transactions.date].formatDefault(),
                    type = it[Transactions.type],
                    category = it[Transactions.category],
                    accountId = it[Transactions.account].value.toString(),
                    status = it[Transactions.status].toString()
                )
            }

            TransactionsResponseDTO(
                transactions = results.toList(),
                totalCount = totalCount
            )
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
                            it[this.date] = transaction.date.toLocalDateTime()
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

        println("Parsed Transactions:")
        parsedTransactions.forEach {
            println("Parsed -> date=${it.date.toLocalDateTime()}, amount=${it.amount.toBigDecimal()}, description='${it.description}'")
        }

        return transaction {
            val triplesToMatch = parsedTransactions.map {
                Triple(it.date.toLocalDateTime(), it.amount.toBigDecimal(), it.description)
            }

            println("\nTriples to match (for query):")
            triplesToMatch.forEach { println(it) }

            val allTransactions = Transaction.find {
                (Transactions.user eq userId) and
                        (Transactions.account eq UUID.fromString(accountId)) and
                        (Transactions.status eq Status.ACTIVE)
            }.map { it.toDomainModel() }

            println("\nAll transactions found in DB:")
            allTransactions.forEach {
                println("DB -> date=${it.date.toLocalDateTime()}, amount=${it.amount}, description='${it.description}'")
            }

            val duplicatedTransactions = Transaction.find {
                (Transactions.user eq userId) and
                        (Transactions.account eq UUID.fromString(accountId)) and
                        (Transactions.status eq Status.ACTIVE) and
                        (Triple(Transactions.date, Transactions.amount, Transactions.description) inList triplesToMatch)
            }.map { it.toDomainModel() }

            println("\nDuplicated transactions found in DB:")
            duplicatedTransactions.forEach {
                println("DB -> date=${it.date.toLocalDateTime()}, amount=${it.amount}, description='${it.description}'")
            }

            val previewItems = parsedTransactions.map { transaction ->
                val isDuplicate = duplicatedTransactions.find {
                    it.date.toLocalDateTime().date == transaction.date.toLocalDateTime().date &&
                    it.amount.toBigDecimal() == transaction.amount.toBigDecimal() &&
                    it.description == transaction.description
                } != null
                if (isDuplicate) {
                    println("Matched Duplicate: ${transaction.description}")
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


