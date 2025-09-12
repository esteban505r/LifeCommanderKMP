package com.esteban.ruano.repository

import com.esteban.ruano.service.BudgetService
import com.esteban.ruano.database.entities.Budget
import com.esteban.ruano.lifecommander.models.finance.BudgetFilters
import com.esteban.ruano.lifecommander.models.finance.TransactionFilters
import com.esteban.ruano.models.finance.BudgetProgressResponseDTO
import com.esteban.ruano.models.finance.BudgetResponseDTO
import com.esteban.ruano.models.finance.TransactionResponseDTO
import com.esteban.ruano.models.finance.TransactionsResponseDTO
import kotlinx.datetime.LocalDate
import java.util.*

class BudgetRepository(
    private val budgetService: BudgetService,
) {
    fun create(
        userId: Int,
        name: String,
        amount: Double,
        category: String,
        startDate: LocalDate,
        endDate: LocalDate? = null
    ): UUID? =
        budgetService.createBudget(userId, name, amount, category, startDate, endDate)

    fun getAll(userId: Int,limit:Int,offset:Int): List<BudgetResponseDTO> = budgetService.getBudgetsByUser(userId, limit,offset)

    fun getAllProgress(
        userId: Int, limit: Int = 10, offset: Int,
        filters: BudgetFilters = BudgetFilters(),
        referenceDate: LocalDate
    ): List<BudgetProgressResponseDTO> {
        return budgetService.getAllWithProgress(userId, limit, offset, filters, referenceDate)
    }

    fun getBudgetTransactions(userId: Int, budgetId: UUID, referenceDate: LocalDate, filters: TransactionFilters,limit:Int,offset:Int): List<TransactionResponseDTO> {
        return budgetService.getBudgetTransactions(userId, budgetId, referenceDate, filters,limit,offset)
    }

    fun getByDateRange(userId: Int, startDate: LocalDate, endDate: LocalDate,limit:Int,offset:Int): List<BudgetResponseDTO> =
        budgetService.getBudgetsByDateRange(userId, startDate, endDate,limit,offset)

    fun update(
        userId: Int,
        budgetId: UUID,
        name: String?,
        amount: Double?,
        category: String?,
        startDate: LocalDate?,
        endDate: LocalDate?
    ): Boolean =
        budgetService.updateBudget(budgetId, userId, name, amount, category, startDate, endDate)

    fun delete(userId: Int, budgetId: UUID): Boolean = budgetService.deleteBudget(budgetId, userId)

    fun getProgress(userId: Int, budgetId: UUID): BudgetProgressResponseDTO =
        budgetService.getBudgetProgress(budgetId, userId)

    fun getUnbudgetedTransactions(userId: Int,referenceDate: LocalDate, filters: TransactionFilters,limit:Int,offset:Int): List<TransactionResponseDTO> {
        return budgetService.getUnbudgetedTransactions(userId,referenceDate,filters,limit,offset)
    }

    fun categorizeUnbudgetedTransactions(userId: Int, referenceDate: LocalDate) : Int{
        return budgetService.categorizeUnbudgetedTransactions(userId,referenceDate)
    }

    fun categorizeAllTransactions(userId: Int, referenceDate: LocalDate): Int {
        return budgetService.categorizeAllTransactions(userId,referenceDate)
    }
}