package com.esteban.ruano.finance_presentation.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.esteban.ruano.core_ui.view_model.BaseViewModel
import com.esteban.ruano.finance_domain.use_case.FinanceUseCases
import com.esteban.ruano.finance_presentation.ui.intent.BudgetIntent
import com.esteban.ruano.finance_presentation.ui.intent.FinanceEffect
import com.esteban.ruano.finance_presentation.ui.viewmodel.state.BudgetState
import com.esteban.ruano.lifecommander.models.finance.Budget
import com.esteban.ruano.lifecommander.models.finance.BudgetFilters
import com.esteban.ruano.utils.DateUIUtils.formatDefault
import com.esteban.ruano.utils.DateUIUtils.getCurrentDateTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toKotlinLocalDate
import javax.inject.Inject

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val financeUseCases: FinanceUseCases
) : BaseViewModel<BudgetIntent, BudgetState, FinanceEffect>() {
    
    override fun createInitialState(): BudgetState {
        return BudgetState()
    }

    override fun handleIntent(intent: BudgetIntent) {
        when (intent) {
            is BudgetIntent.GetBudgets -> getBudgets(intent.reset)
            is BudgetIntent.AddBudget -> addBudget(intent.budget)
            is BudgetIntent.UpdateBudget -> updateBudget(intent.budget)
            is BudgetIntent.DeleteBudget -> deleteBudget(intent.id)
            is BudgetIntent.GetBudgetProgress -> getBudgetProgress(intent.budgetId)
            is BudgetIntent.GetBudgetTransactions -> getBudgetTransactions(intent.budgetId, intent.refresh)
            is BudgetIntent.ChangeBudgetFilters -> changeBudgetFilters(intent.filters)
            is BudgetIntent.ChangeBudgetBaseDate -> changeBudgetBaseDate(intent.date.toKotlinLocalDate())
            is BudgetIntent.CategorizeAll -> categorizeAll()
            is BudgetIntent.CategorizeUnbudgeted -> categorizeUnbudgeted()
        }
    }

    fun getBudgets(reset: Boolean = false) {
        viewModelScope.launch {
            emitState { currentState.copy(isLoading = true, error = null) }
            
            financeUseCases.getBudgets(
                filters = currentState.budgetFilters,
                referenceDate = currentState.budgetBaseDate.ifEmpty { 
                    getCurrentDateTime(
                        TimeZone.currentSystemDefault()
                    ).date.formatDefault()
                }
            ).fold(
                onSuccess = { budgets ->
                    emitState {
                        currentState.copy(
                            budgets = budgets,
                            isLoading = false
                        )
                    }
                },
                onFailure = { error ->
                    emitState {
                        currentState.copy(
                            error = error.message,
                            isLoading = false
                        )
                    }
                }
            )
        }
    }

    fun addBudget(budget: Budget) {
        viewModelScope.launch {
            emitState { currentState.copy(isLoading = true, error = null) }
            
            financeUseCases.addBudget(budget).fold(
                onSuccess = {
                    getBudgets()
                },
                onFailure = { error ->
                    emitState {
                        currentState.copy(
                            error = error.message,
                            isLoading = false
                        )
                    }
                }
            )
        }
    }

    fun updateBudget(budget: Budget) {
        viewModelScope.launch {
            emitState { currentState.copy(isLoading = true, error = null) }
            
            financeUseCases.updateBudget(budget).fold(
                onSuccess = {
                    getBudgets()
                },
                onFailure = { error ->
                    emitState {
                        currentState.copy(
                            error = error.message,
                            isLoading = false
                        )
                    }
                }
            )
        }
    }

    fun deleteBudget(id: String) {
        viewModelScope.launch {
            emitState { currentState.copy(isLoading = true, error = null) }
            
            financeUseCases.deleteBudget(id).fold(
                onSuccess = {
                    emitState {
                        currentState.copy(
                            budgets = currentState.budgets.filter { it.budget.id != id },
                            isLoading = false
                        )
                    }
                },
                onFailure = { error ->
                    emitState {
                        currentState.copy(
                            error = error.message,
                            isLoading = false
                        )
                    }
                }
            )
        }
    }

    fun changeBudgetFilters(filters: BudgetFilters) {
        emitState {
            currentState.copy(
                budgetFilters = filters
            )
        }
        getBudgets()
    }

    fun changeBudgetBaseDate(date: kotlinx.datetime.LocalDate) {
        emitState {
            currentState.copy(
                budgetBaseDate = date.formatDefault()
            )
        }
        getBudgets()
    }

    fun getBudgetProgress(budgetId: String) {
        viewModelScope.launch {
            financeUseCases.getBudgetProgress(budgetId).fold(
                onSuccess = { updatedProgress ->
                    emitState {
                        currentState.copy(
                            budgets = currentState.budgets.map { budgetProgress ->
                                if (budgetProgress.budget.id == budgetId) {
                                    updatedProgress
                                } else {
                                    budgetProgress
                                }
                            }
                        )
                    }
                },
                onFailure = { error ->
                    emitState {
                        currentState.copy(
                            error = error.message
                        )
                    }
                }
            )
        }
    }

    fun getBudgetTransactions(budgetId: String, refresh: Boolean = false) {
        viewModelScope.launch {
            try {
                emitState { currentState.copy(isLoading = true, error = null) }
                financeUseCases.getBudgetTransactions(
                    budgetId = budgetId,
                    referenceDate = getCurrentDateTime(
                        TimeZone.currentSystemDefault()
                    ).date.formatDefault(),
                    filters = currentState.transactionFilters
                ).fold(
                    onSuccess = { transactions ->
                        emitState {
                            currentState.copy(
                                budgetTransactions = transactions,
                                isLoading = false
                            )
                        }
                    },
                    onFailure = { error ->
                        emitState {
                            currentState.copy(
                                error = error.message,
                                isLoading = false
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                emitState {
                    currentState.copy(
                        error = e.message,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun categorizeAll() {
        viewModelScope.launch {
            try {
                emitState { currentState.copy(isLoading = true, error = null) }
                financeUseCases.categorizeAllTransactions(
                    referenceDate = getCurrentDateTime(
                        TimeZone.currentSystemDefault()
                    ).date.formatDefault()
                ).fold(
                    onSuccess = {
                        getBudgets()
                    },
                    onFailure = { error ->
                        emitState {
                            currentState.copy(
                                error = error.message,
                                isLoading = false
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                emitState {
                    currentState.copy(
                        error = e.message,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun categorizeUnbudgeted() {
        viewModelScope.launch {
            try {
                emitState { currentState.copy(isLoading = true, error = null) }
                financeUseCases.categorizeUnbudgeted(
                    referenceDate = getCurrentDateTime(
                        TimeZone.currentSystemDefault()
                    ).date.formatDefault()
                ).fold(
                    onSuccess = {
                        getBudgets()
                    },
                    onFailure = { error ->
                        emitState {
                            currentState.copy(
                                error = error.message,
                                isLoading = false
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                emitState {
                    currentState.copy(
                        error = e.message,
                        isLoading = false
                    )
                }
            }
        }
    }
}

