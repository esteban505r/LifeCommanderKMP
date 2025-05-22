package com.esteban.ruano.service

import com.esteban.ruano.database.converters.toDomainModel
import com.esteban.ruano.database.converters.toResponseDTO
import com.esteban.ruano.database.entities.*
import com.esteban.ruano.database.models.Status
import com.esteban.ruano.lifecommander.models.finance.BudgetFilters
import com.esteban.ruano.lifecommander.models.finance.Category
import com.esteban.ruano.lifecommander.models.finance.TransactionFilters
import com.esteban.ruano.lifecommander.utils.getCurrentPeriod
import com.esteban.ruano.lifecommander.utils.getCurrentPeriodForUnbudgeted
import com.esteban.ruano.models.finance.*
import com.esteban.ruano.utils.DateUIUtils.formatDefault
import com.esteban.ruano.utils.DateUtils.getPeriodEndDate
import com.esteban.ruano.utils.addSortOrder
import com.esteban.ruano.utils.buildTransactionFilters
import com.esteban.ruano.utils.toSortOrder
import com.lifecommander.finance.model.TransactionType
import com.lifecommander.models.Frequency
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toLocalDate
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.select

import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import kotlin.math.absoluteValue

class BudgetService : BaseService() {
    fun createBudget(
        userId: Int,
        name: String,
        amount: Double,
        category: String,
        startDate: LocalDate,
        endDate: LocalDate? = null
    ): UUID? {
        return transaction {
            Budgets.insertOperation(userId) {
                insert {
                    it[this.name] = name
                    it[this.amount] = amount.toBigDecimal()
                    it[this.category] = category
                    it[this.startDate] = startDate
                    it[this.endDate] = endDate
                    it[this.user] = userId
                }.resultedValues?.firstOrNull()?.getOrNull(this.id)?.value
            }
        }
    }

    fun getBudgetsByUser(userId: Int): List<BudgetResponseDTO> {
        return transaction {
            Budget.find { Budgets.user eq userId }
                .map { it.toResponseDTO() }
        }
    }

    fun getBudgetsByDateRange(userId: Int, startDate: LocalDate, endDate: LocalDate): List<BudgetResponseDTO> {
        return transaction {
            Budget.find {
                (Budgets.user eq userId) and
                        (Budgets.startDate greaterEq startDate)
                            .and(Budgets.endDate lessEq endDate)

            }.map { it.toResponseDTO() }
        }
    }


    fun getAllWithProgress(
        userId: Int,
        limit: Int = 10,
        offset: Int = 0,
        filters: BudgetFilters = BudgetFilters(),
        referenceDate: LocalDate
    ): List<BudgetProgressResponseDTO> {
        return transaction {
            // 1. Build the base query with filters (excluding startDate and endDate)
            var baseQuery = Budgets.selectAll().where { Budgets.user eq userId }

            filters.searchPattern?.let { pattern ->
                baseQuery = baseQuery.andWhere { Budgets.name like "%$pattern%" }
            }

            filters.categories?.let { categories ->
                baseQuery = baseQuery.andWhere { Budgets.category inList categories }
            }

            filters.minAmount?.let { min ->
                baseQuery = baseQuery.andWhere { Budgets.amount greaterEq min.toBigDecimal() }
            }

            filters.maxAmount?.let { max ->
                baseQuery = baseQuery.andWhere { Budgets.amount lessEq max.toBigDecimal() }
            }

            // 2. Apply ordering and pagination
            val paginatedQuery = baseQuery
                .orderBy(Budgets.startDate to SortOrder.DESC)
                .limit(limit, offset.toLong())

            // 3. Fetch and map each budget
            val budgetedResults = paginatedQuery.map { row ->
                val budget = Budget.wrapRow(row)
                val (periodStart, periodEnd) = getCurrentPeriod(budget.toDomainModel(), referenceDate)

                val spent = Transaction.find {
                    (Transactions.user eq userId) and
                            (Transactions.category.lowerCase() eq budget.category.lowercase()) and
                            (Transactions.date.date() greaterEq periodStart) and
                            (Transactions.date.date() lessEq periodEnd)
                }.sumOf { it.amount.toDouble() }

                BudgetProgressResponseDTO(
                    budget = budget.toResponseDTO(),
                    spent = spent.absoluteValue
                )
            }.filter { dto ->
                filters.isOverBudget?.let { isOver ->
                    (dto.spent > dto.budget.amount) == isOver
                } ?: true
            }

            // 4. Find categories already budgeted
            val budgetedCategories = baseQuery
                .map { it[Budgets.category] }
                .distinct()

            // 5. Determine the current period for unbudgeted transactions
            val (periodStart, periodEnd) = getCurrentPeriodForUnbudgeted(referenceDate)

            // 6. Get unbudgeted transactions within the current period
            var unbudgetedQuery = Transactions.selectAll().where {
                (Transactions.user eq userId) and
                        (Transactions.category notInList budgetedCategories) and
                        (Transactions.type eq TransactionType.EXPENSE) and
                        (Transactions.date.date() greaterEq periodStart) and
                        (Transactions.date.date() lessEq periodEnd)
            }

            filters.minAmount?.let { min ->
                unbudgetedQuery = unbudgetedQuery.andWhere { Transactions.amount greaterEq min.toBigDecimal() }
            }

            filters.maxAmount?.let { max ->
                unbudgetedQuery = unbudgetedQuery.andWhere { Transactions.amount lessEq max.toBigDecimal() }
            }

            val unbudgetedSpent = unbudgetedQuery.sumOf { it[Transactions.amount].toDouble() }

            val unbudgetedResult = if (unbudgetedSpent.absoluteValue > 0) {
                listOf(
                    BudgetProgressResponseDTO(
                        budget = BudgetResponseDTO(
                            id = "unbudgeted",
                            name = "Unbudgeted",
                            category = Category.UNBUDGETED.name,
                            amount = 0.0,
                            startDate = periodStart.formatDefault(),
                            endDate = periodEnd.formatDefault(),
                            frequency = "NONE"
                        ),
                        spent = unbudgetedSpent.absoluteValue
                    )
                )
            } else emptyList()

            // 7. Return all combined
            return@transaction budgetedResults + unbudgetedResult
        }
    }


    fun getBudgetProgress(budgetId: UUID, userId: Int): BudgetProgressResponseDTO {
        return transaction {
            val budget = Budget.findById(budgetId)
            val spent = if (budget != null && budget.user.id.value == userId) {
                val spent = Transaction.find {
                    (Transactions.user eq userId) and
                            (Transactions.category eq budget.category) and
                            (Transactions.date.date() greaterEq budget.startDate)
                }.sumOf { it.amount.toDouble() }
                spent / budget.amount.toDouble()
            } else {
                0.0
            }
            budget?.let {
                BudgetProgressResponseDTO(
                    budget = it.toResponseDTO(),
                    spent = spent
                )
            } ?: throw Exception("Budget not found")
        }
    }

    fun updateBudget(
        budgetId: UUID,
        userId: Int,
        name: String? = null,
        amount: Double? = null,
        category: String? = null,
        startDate: LocalDate? = null,
        endDate: LocalDate? = null
    ): Boolean {
        return transaction {
            val budget = Budget.findById(budgetId)
            if (budget != null && budget.user.id.value == userId) {
                name?.let { budget.name = it }
                amount?.let { budget.amount = it.toBigDecimal() }
                category?.let { budget.category = it }
                startDate?.let { budget.startDate = it }
                endDate?.let { budget.endDate = it }
                true
            } else {
                false
            }
        }
    }

    fun deleteBudget(budgetId: UUID, userId: Int): Boolean {
        return transaction {
            val budget = Budget.findById(budgetId)
            if (budget != null && budget.user.id.value == userId) {
                budget.status = Status.INACTIVE
                true
            } else {
                false
            }
        }
    }

    fun getBudgetTransactions(userId: Int, budgetId: UUID, filters: TransactionFilters): List<TransactionResponseDTO> {
        return transaction {
            val budget = Budget.findById(budgetId)
            if (budget != null && budget.user.id.value == userId) {
                Transactions.selectAll()
                    .where {
                        buildTransactionFilters(
                            userId,
                            filters
                        )
                    }.andWhere {
                        (Transactions.category eq budget.category) and
                                (Transactions.date.date() greaterEq budget.startDate)
                    }
                    .addSortOrder(
                        filters.amountSortOrder,
                        Transactions.amount,
                    )
                    .map { Transaction.wrapRow(it).toResponseDTO() }
            } else {
                emptyList()
            }
        }
    }


    fun getUnbudgetedTransactions(
        userId: Int,
        referenceDate: LocalDate,
        filters: TransactionFilters
    ): List<TransactionResponseDTO> {
        return transaction {
            // Retrieve all budgets for the user
            val budgets = Budget.find { Budgets.user eq userId }

            // Determine the current period for each budget
            val budgetPeriods = budgets.map { budget ->
                val (periodStart, periodEnd) = getCurrentPeriod(budget.toDomainModel(), referenceDate)
                Triple(budget.category.lowercase(), periodStart, periodEnd)
            }

            // Extract the list of budgeted categories
            val budgetedCategories = budgetPeriods.map { it.first }.distinct()

            // Build the base query for unbudgeted transactions
            var unbudgetedQuery = Transactions.selectAll()
                .where {
                    buildTransactionFilters(
                        userId,
                        filters
                    )
                }.andWhere {
                    (Transactions.category.lowerCase() notInList budgetedCategories) and
                            (Transactions.type eq TransactionType.EXPENSE)
                }.addSortOrder(
                    filters.amountSortOrder,
                    Transactions.amount,
                )

            // Apply date filters based on the earliest and latest period dates
            val periodStartDates = budgetPeriods.map { it.second }
            val periodEndDates = budgetPeriods.map { it.third }

            if (periodStartDates.isNotEmpty() && periodEndDates.isNotEmpty()) {
                val overallStartDate = periodStartDates.minOrNull() ?: referenceDate
                val overallEndDate = periodEndDates.maxOrNull() ?: referenceDate

                unbudgetedQuery = unbudgetedQuery.andWhere {
                    (Transactions.date.date() greaterEq overallStartDate) and
                            (Transactions.date.date() lessEq overallEndDate)
                }
            }

            // Execute the query and map the results
            unbudgetedQuery.map { row ->
                Transaction.wrapRow(row).toResponseDTO()
            }
        }
    }

    fun categorizeUnbudgetedTransactions(userId: Int, referenceDate: LocalDate): Int {
        return transaction {
            // Get unbudgeted transactions
            val unbudgetedTransactions = getUnbudgetedTransactions(userId, referenceDate, TransactionFilters())

            // Get all category keywords for the user
            val categoryKeywords = CategoryKeyword.find { CategoryKeywords.user eq userId }
                .groupBy { it.category.name.lowercase() }
                .mapValues { (_, keywords) -> keywords.map { it.keyword.lowercase() } }

            var categorizedCount = 0

            // Process each unbudgeted transaction
            unbudgetedTransactions.forEach { transaction ->
                val description = transaction.description.lowercase()

                // Find matching category based on keywords
                val matchingCategory = categoryKeywords.entries.find { (_, keywords) ->
                    keywords.any { keyword -> description.lowercase().contains(keyword.lowercase()) }
                }?.key

                if (matchingCategory != null) {
                    // Update transaction category
                    Transaction.findById(UUID.fromString(transaction.id))?.let { dbTransaction ->
                        dbTransaction.category = matchingCategory.uppercase()
                        categorizedCount++
                    }
                }
            }

            categorizedCount
        }
    }

    fun categorizeAllTransactions(userId: Int, referenceDate: LocalDate): Int {
        return transaction {
            // Get all transactions for the date range
            val (periodStart, periodEnd) = getCurrentPeriodForUnbudgeted(referenceDate)

            val transactions = Transaction.find {
                (Transactions.user eq userId) and
                        (Transactions.date.date() greaterEq periodStart) and
                        (Transactions.date.date() lessEq periodEnd)
            }.map { it.toResponseDTO() }

            // Get all category keywords for the user
            val categoryKeywords = CategoryKeyword.find { CategoryKeywords.user eq userId }
                .groupBy { it.category.name.lowercase() }
                .mapValues { (_, keywords) -> keywords.map { it.keyword.lowercase() } }

            var categorizedCount = 0

            // Process each transaction
            transactions.forEach { transaction ->
                val description = transaction.description.lowercase()

                // Find matching category based on keywords
                val matchingCategory = categoryKeywords.entries.find { (_, keywords) ->
                    keywords.any { keyword -> description.lowercase().contains(keyword.lowercase()) }
                }?.key

                if (matchingCategory != null) {
                    // Update transaction category
                    Transaction.findById(UUID.fromString(transaction.id))?.let { dbTransaction ->
                        dbTransaction.category = matchingCategory.uppercase()
                        categorizedCount++
                    }
                }
            }

            categorizedCount
        }
    }
} 