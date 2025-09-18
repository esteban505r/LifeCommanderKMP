package com.esteban.ruano.service

import com.esteban.ruano.database.converters.toDomainModel
import com.esteban.ruano.database.converters.toResponseDTO
import com.esteban.ruano.database.entities.*
import com.esteban.ruano.database.models.Status
import com.esteban.ruano.lifecommander.models.finance.TransactionFilters
import com.esteban.ruano.models.finance.*
import com.esteban.ruano.utils.*
import com.esteban.ruano.utils.DateUIUtils.formatDefault
import com.esteban.ruano.utils.DateUIUtils.toLocalDate
import com.esteban.ruano.utils.DateUIUtils.toLocalDateTime
import com.lifecommander.finance.model.TransactionImportPreview
import com.lifecommander.finance.model.TransactionImportPreviewItem
import com.lifecommander.finance.model.TransactionType
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.atTime
import kotlinx.datetime.plus
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.core.lessEq
import org.jetbrains.exposed.v1.datetime.date
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.*

class TransactionService() : BaseService() {
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


    fun getForecastedTransactions(
        userId: Int,
        fromDate: LocalDate,
        toDate: LocalDate,
        offset: Int,
        limit: Int,
        sortOrder: SortOrder = SortOrder.DESC
    ): TransactionsResponseDTO {
        val committed = transaction {
            Transactions.selectAll().where{
                (Transactions.user eq userId) and
                        (Transactions.status eq Status.ACTIVE) and
                        (Transactions.date.date() greaterEq fromDate) and
                        (Transactions.date.date() lessEq toDate)
            }
                .map {
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
        }

        val scheduled = transaction {
            ScheduledTransaction.find {
                (ScheduledTransactions.user eq userId) and
                        (ScheduledTransactions.status eq Status.ACTIVE)
            }.flatMap { scheduled ->
                generateOccurrencesBetween(
                    startDate = scheduled.startDate.date,
                    frequency = scheduled.frequency,
                    interval = scheduled.interval,
                    from = fromDate,
                    to = toDate
                ).map { date ->
                    TransactionResponseDTO(
                        id = "scheduled-${scheduled.id.value}-$date",
                        amount = scheduled.amount.toDouble(),
                        description = "[Planned] ${scheduled.description}",
                        date = date.atTime(0,0).formatDefault(),
                        type = scheduled.type,
                        category = scheduled.category,
                        accountId = scheduled.account.id.value.toString(),
                        status = "PLANNED"
                    )
                }
            }
        }

        val all = if (sortOrder == SortOrder.ASC) {
            (committed + scheduled).sortedBy { it.date }
        } else {
            (committed + scheduled).sortedByDescending { it.date }
        }

        val paginated = all.drop(offset).take(limit)

        return TransactionsResponseDTO(
            transactions = paginated,
            totalCount = all.size.toLong()
        )
    }



    fun getTransactionsByUser(
        userId: Int,
        limit: Int = 100,
        offset: Int = 0,
        filters: TransactionFilters = TransactionFilters(),
        scheduledBaseDate: LocalDate? = null
    ): TransactionsResponseDTO {
        if (scheduledBaseDate != null) {
            return getForecastedTransactions(
                userId = userId,
                fromDate = filters.startDate?.toLocalDate() ?: scheduledBaseDate,
                toDate = filters.endDate?.toLocalDate() ?: scheduledBaseDate.plus(DatePeriod(days = 30)),
                offset = offset,
                limit = limit,
                sortOrder = filters.amountSortOrder.toSortOrder() ?: SortOrder.DESC
            )
        }

        return transaction {
            val baseQuery = Transactions.selectAll().where {
                buildTransactionFilters(userId = userId, filters = filters)
            }

            val totalCount = baseQuery.count()

            val paginatedResults = baseQuery
                .addSortOrder(
                    filters.amountSortOrder,
                    absAmount,
                    defaultSortOrder = Transactions.date to SortOrder.DESC
                ).limit(limit).offset(offset.toLong()*limit)

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

    fun expandScheduledTransactions(
        userId: Int,
        fromDate: LocalDate,
        toDate: LocalDate
    ): List<TransactionResponseDTO> = transaction {
        ScheduledTransaction.find {
            (ScheduledTransactions.user eq userId) and
                    (ScheduledTransactions.status eq Status.ACTIVE)
        }.flatMap { scheduled ->
            val frequency = scheduled.frequency
            val interval = scheduled.interval
            val startDate = scheduled.startDate

            val occurrenceDates = generateOccurrencesBetween(
                startDate = startDate.date,
                frequency = frequency,
                interval = interval,
                from = fromDate,
                to = toDate
            )

            occurrenceDates.map { date ->
                TransactionResponseDTO(
                    id = "scheduled-${scheduled.id.value}-$date",
                    amount = scheduled.amount.toDouble(),
                    description = "[Planned] ${scheduled.description}",
                    date = date.atTime(0,0).formatDefault(),
                    type = scheduled.type,
                    category = scheduled.category,
                    accountId = scheduled.account.id.value.toString(),
                    status = "PLANNED"
                )
            }
        }
    }

    fun getTransactionsByAccount(accountId: UUID): List<TransactionResponseDTO> {
        return transaction {
            Transaction.find { Transactions.account eq accountId and (Transactions.status eq Status.ACTIVE) }
                .map { it.toResponseDTO() }
        }
    }


    fun getTransaction(transactionId: UUID, userId: Int): TransactionResponseDTO? {
        return transaction {
            Transaction.find {
                (Transactions.id eq transactionId) and (Transactions.user eq userId) and (Transactions.status eq Status.ACTIVE)
            }
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
                transaction.status = Status.DELETED
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
        val transactions = TransactionParser.parseTransactions(userId,text, accountId)
        return importTransactions(userId, transactions, skipDuplicates)
    }

    fun previewTransactionImport(
        userId: Int,
        text: String,
        accountId: String
    ): TransactionImportPreview {
        val parsedTransactions = TransactionParser.parseTransactions(userId,text, accountId)

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


