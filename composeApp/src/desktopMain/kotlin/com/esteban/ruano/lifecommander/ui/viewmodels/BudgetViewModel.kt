package com.esteban.ruano.lifecommander.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esteban.ruano.lifecommander.models.finance.Budget
import com.esteban.ruano.lifecommander.models.finance.BudgetFilters
import com.esteban.ruano.lifecommander.services.finance.FinanceService
import com.esteban.ruano.lifecommander.utils.PaginatedDataFetcher
import com.esteban.ruano.utils.DateUIUtils.formatDefault
import com.esteban.ruano.utils.DateUIUtils.getCurrentDateTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

data class BudgetState(
    val budgets: List<com.esteban.ruano.lifecommander.models.finance.BudgetProgress> = emptyList(),
    val budgetFilters: BudgetFilters = BudgetFilters(),
    val budgetBaseDate: String = getCurrentDateTime(
        TimeZone.currentSystemDefault()
    ).date.formatDefault(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class BudgetViewModel(
    private val service: FinanceService
) : ViewModel() {

    private val _state = MutableStateFlow(BudgetState())
    val state: StateFlow<BudgetState> = _state.asStateFlow()

    private val budgetsFetcher = PaginatedDataFetcher(
        pageSize = 20,
        fetchData = { page, pageSize ->
            service.getBudgetsWithProgress(
                filters = _state.value.budgetFilters,
                referenceDate = _state.value.budgetBaseDate,
                limit = pageSize,
                offset = page
            )
        }
    )

    init {
        viewModelScope.launch {
            budgetsFetcher.data.collect { budgets ->
                _state.value = _state.value.copy(
                    budgets = budgets,
                    isLoading = false
                )
            }
        }
    }

    fun getBudgets(reset: Boolean = false) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)
                budgetsFetcher.loadNextPage(reset)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun addBudget(budget: Budget) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)
                service.addBudget(budget)
                getBudgets(reset = true)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun updateBudget(budget: Budget) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)
                service.updateBudget(budget)
                getBudgets(reset = true)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun deleteBudget(id: String) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)
                service.deleteBudget(id)
                _state.value = _state.value.copy(
                    budgets = _state.value.budgets.filter { it.budget.id != id },
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun changeBudgetFilters(filters: BudgetFilters) {
        _state.value = _state.value.copy(
            budgetFilters = filters
        )
        getBudgets(reset = true)
    }

    fun changeBudgetBaseDate(date: LocalDate) {
        _state.value = _state.value.copy(
            budgetBaseDate = date.formatDefault()
        )
        getBudgets(reset = true)
    }

    fun getBudgetProgress(budgetId: String) {
        viewModelScope.launch {
            try {
                service.getBudgetProgress(budgetId)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message
                )
            }
        }
    }

    fun categorizeAll() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)
                service.categorizeAllTransactions(
                    referenceDate = getCurrentDateTime(
                        TimeZone.currentSystemDefault()
                    ).date.formatDefault()
                )
                getBudgets(reset = true)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun categorizeUnbudgeted() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)
                service.categorizeUnbudgeted(
                    referenceDate = getCurrentDateTime(
                        TimeZone.currentSystemDefault()
                    ).date.formatDefault()
                )
                getBudgets(reset = true)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }
}

