package com.esteban.ruano.service

import com.esteban.ruano.database.converters.toResponseDTO
import com.esteban.ruano.database.entities.*
import com.esteban.ruano.database.models.Status
import com.esteban.ruano.models.finance.*
import com.esteban.ruano.utils.DateUtils.getPeriodEndDate
import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.kotlin.datetime.date
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
                    .and (Budgets.endDate lessEq endDate)

            }.map { it.toResponseDTO() }
        }
    }

    fun getAllWithProgress(userId: Int): List<BudgetProgressResponseDTO> {
        return transaction {
            Budget.find { Budgets.user eq userId }
                .map { budget ->
                    val spent = Transaction.find {
                        (Transactions.user eq userId) and
                        (Transactions.category eq budget.category) and
                        (Transactions.date.date() greaterEq budget.startDate)
                        (Transactions.date.date() lessEq (budget.endDate?: getPeriodEndDate(budget.startDate, budget.frequency)))
                    }.sumOf { it.amount.toDouble() }
                    BudgetProgressResponseDTO(
                        budget = budget.toResponseDTO(),
                        spent = spent.absoluteValue
                    )
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
} 