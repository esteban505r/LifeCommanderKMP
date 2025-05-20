package com.esteban.ruano.service

import com.esteban.ruano.database.converters.toResponseDTO
import com.esteban.ruano.database.entities.*
import com.esteban.ruano.database.models.Status
import com.esteban.ruano.lifecommander.models.finance.BudgetFilters
import com.esteban.ruano.models.finance.*
import com.esteban.ruano.utils.DateUtils.getPeriodEndDate
import com.lifecommander.models.Frequency
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
        filters: BudgetFilters = BudgetFilters()
    ): List<BudgetProgressResponseDTO> {
        return transaction {
            var baseQuery = Budgets.selectAll().where { Budgets.user eq userId }

            filters.searchPattern?.let { pattern ->
                baseQuery = baseQuery.andWhere { Budgets.name like "%$pattern%" }
            }

            filters.categories?.let { categories ->
                baseQuery = baseQuery.andWhere { Budgets.category inList categories }
            }

            filters.startDate?.let { start ->
                baseQuery = baseQuery.andWhere { Budgets.startDate greaterEq start.toLocalDate() }
            }

            filters.endDate?.let { end ->
                baseQuery = baseQuery.andWhere { Budgets.endDate lessEq end.toLocalDate() }
            }

            filters.minAmount?.let { min ->
                baseQuery = baseQuery.andWhere { Budgets.amount greaterEq min.toBigDecimal() }
            }

            filters.maxAmount?.let { max ->
                baseQuery = baseQuery.andWhere { Budgets.amount lessEq max.toBigDecimal() }
            }

            val paginatedQuery = baseQuery
                .orderBy(Budgets.startDate to SortOrder.DESC)
                .limit(limit, offset.toLong())

            paginatedQuery.map { row ->
                val budget = Budget.wrapRow(row)
                val effectiveEndDate = budget.endDate ?: getPeriodEndDate(budget.startDate, budget.frequency)

                val spent = Transaction.find {
                    (Transactions.user eq userId) and
                            (Transactions.category eq budget.category) and
                            (Transactions.date.date() greaterEq budget.startDate) and
                            (Transactions.date.date() lessEq effectiveEndDate)
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

    fun getBudgetTransactions(userId: Int, budgetId: UUID): List<TransactionResponseDTO> {
        return transaction {
            val budget = Budget.findById(budgetId)
            if (budget != null && budget.user.id.value == userId) {
                Transaction.find {
                    (Transactions.user eq userId) and
                            (Transactions.category eq budget.category) and
                            (Transactions.date.date() greaterEq budget.startDate)
                }.map { it.toResponseDTO() }
            } else {
                emptyList()
            }
        }
    }
} 