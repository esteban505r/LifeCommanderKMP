package com.esteban.ruano.repository

import com.esteban.ruano.service.BudgetService
import com.esteban.ruano.database.entities.Budget
import com.esteban.ruano.models.finance.BudgetProgressResponseDTO
import com.esteban.ruano.models.finance.BudgetResponseDTO
import kotlinx.datetime.LocalDate
import java.util.*

class BudgetRepository(private val service: BudgetService) {
    fun create(userId: Int, name: String, amount: Double, category: String, startDate: LocalDate, endDate: LocalDate? = null): UUID? =
        service.createBudget(userId, name, amount, category, startDate, endDate)

    fun getAll(userId: Int): List<BudgetResponseDTO> = service.getBudgetsByUser(userId)

    fun getAllProgress(userId: Int): List<BudgetProgressResponseDTO> {
        return service.getAllWithProgress(userId)
    }

    fun getByDateRange(userId: Int, startDate: LocalDate, endDate: LocalDate): List<BudgetResponseDTO> =
        service.getBudgetsByDateRange(userId, startDate, endDate)

    fun update(userId: Int, budgetId: UUID, name: String?, amount: Double?, category: String?, startDate: LocalDate?, endDate: LocalDate?): Boolean =
        service.updateBudget(budgetId, userId, name, amount, category, startDate, endDate)

    fun delete(userId: Int, budgetId: UUID): Boolean = service.deleteBudget(budgetId, userId)

    fun getProgress(userId: Int, budgetId: UUID): BudgetProgressResponseDTO = service.getBudgetProgress(budgetId, userId)
}