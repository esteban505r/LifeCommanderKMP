package com.esteban.ruano.service

import com.esteban.ruano.database.converters.toResponseDTO
import com.esteban.ruano.database.entities.*
import com.esteban.ruano.database.models.Status
import com.esteban.ruano.database.models.TransactionType
import com.esteban.ruano.models.finance.*
import jdk.internal.vm.vector.VectorSupport.insert
import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class BudgetService : BaseService() {
    fun createBudget(
        userId: Int,
        name: String,
        amount: Double,
        category: String,
        startDate: LocalDate,
        endDate: LocalDate
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
                (Budgets.startDate lessEq endDate) and 
                (Budgets.endDate greaterEq startDate)
            }.map { it.toResponseDTO() }
        }
    }

    fun getBudgetProgress(budgetId: UUID, userId: Int): Double {
        return transaction {
            val budget = Budget.findById(budgetId)
            if (budget != null && budget.user.id.value == userId) {
                val spent = Transaction.find { 
                    (Transactions.user eq userId) and 
                    (Transactions.category eq budget.category) and
                    (Transactions.date.date() greaterEq budget.startDate) and
                    (Transactions.date.date() lessEq budget.endDate)
                }.sumOf { it.amount.toDouble() }
                spent / budget.amount.toDouble()
            } else {
                0.0
            }
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