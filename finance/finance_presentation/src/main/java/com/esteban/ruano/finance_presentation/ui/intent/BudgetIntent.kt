package com.esteban.ruano.finance_presentation.ui.intent

import com.esteban.ruano.core_ui.view_model.UserIntent
import com.esteban.ruano.lifecommander.models.finance.Budget
import com.esteban.ruano.lifecommander.models.finance.BudgetFilters
import kotlinx.datetime.LocalDate

sealed class BudgetIntent : UserIntent {
    data class GetBudgets(val reset: Boolean = false) : BudgetIntent()
    data class AddBudget(val budget: Budget) : BudgetIntent()
    data class UpdateBudget(val budget: Budget) : BudgetIntent()
    data class DeleteBudget(val id: String) : BudgetIntent()
    data class GetBudgetProgress(val budgetId: String) : BudgetIntent()
    data class GetBudgetTransactions(val budgetId: String, val refresh: Boolean = false) : BudgetIntent()
    data class ChangeBudgetFilters(val filters: BudgetFilters) : BudgetIntent()
    data class ChangeBudgetBaseDate(val date: LocalDate) : BudgetIntent()
    object CategorizeAll : BudgetIntent()
    object CategorizeUnbudgeted : BudgetIntent()
}

