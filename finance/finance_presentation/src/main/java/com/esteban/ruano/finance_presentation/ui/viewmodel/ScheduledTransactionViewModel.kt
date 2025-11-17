package com.esteban.ruano.finance_presentation.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.esteban.ruano.core_ui.view_model.BaseViewModel
import com.esteban.ruano.finance_domain.use_case.FinanceUseCases
import com.esteban.ruano.finance_presentation.ui.intent.FinanceEffect
import com.esteban.ruano.finance_presentation.ui.intent.ScheduledTransactionIntent
import com.esteban.ruano.finance_presentation.ui.viewmodel.state.ScheduledTransactionState
import com.esteban.ruano.lifecommander.models.finance.TransactionFilters
import com.lifecommander.finance.model.ScheduledTransaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScheduledTransactionViewModel @Inject constructor(
    private val financeUseCases: FinanceUseCases
) : BaseViewModel<ScheduledTransactionIntent, ScheduledTransactionState, FinanceEffect>() {
    
    override fun createInitialState(): ScheduledTransactionState {
        return ScheduledTransactionState()
    }

    override fun handleIntent(intent: ScheduledTransactionIntent) {
        when (intent) {
            is ScheduledTransactionIntent.GetScheduledTransactions -> getScheduledTransactions(intent.refresh)
            is ScheduledTransactionIntent.AddScheduledTransaction -> addScheduledTransaction(intent.transaction)
            is ScheduledTransactionIntent.UpdateScheduledTransaction -> updateScheduledTransaction(intent.transaction)
            is ScheduledTransactionIntent.DeleteScheduledTransaction -> deleteScheduledTransaction(intent.id)
            is ScheduledTransactionIntent.ChangeTransactionFilters -> changeTransactionFilters(intent.filters) {}
        }
    }

    fun getScheduledTransactions(refresh: Boolean) {
        viewModelScope.launch {
            emitState {
                currentState.copy(
                    isLoading = true,
                    error = null,
                    currentPage = if (refresh) 0 else currentState.currentPage
                )
            }

            financeUseCases.getScheduledTransactions(
                filter = null,
                page = currentState.currentPage,
                limit = currentState.pageSize,
                filters = currentState.transactionFilters
            ).fold(
                onSuccess = { response ->
                    emitState {
                        currentState.copy(
                            scheduledTransactions = response.transactions,
                            totalScheduledTransactions = response.totalCount,
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

    fun addScheduledTransaction(transaction: ScheduledTransaction) {
        viewModelScope.launch {
            emitState { currentState.copy(isLoading = true, error = null) }
            
            financeUseCases.addScheduledTransaction(transaction).fold(
                onSuccess = {
                    getScheduledTransactions(refresh = true)
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

    fun updateScheduledTransaction(transaction: ScheduledTransaction) {
        viewModelScope.launch {
            emitState { currentState.copy(isLoading = true, error = null) }
            
            financeUseCases.updateScheduledTransaction(transaction).fold(
                onSuccess = {
                    getScheduledTransactions(refresh = true)
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

    fun deleteScheduledTransaction(id: String) {
        viewModelScope.launch {
            emitState { currentState.copy(isLoading = true, error = null) }
            
            financeUseCases.deleteScheduledTransaction(id).fold(
                onSuccess = {
                    getScheduledTransactions(refresh = true)
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

    fun changeTransactionFilters(filters: TransactionFilters, onSuccess: () -> Unit) {
        emitState {
            currentState.copy(
                transactionFilters = filters,
                currentPage = 0
            )
        }
        onSuccess.invoke()
    }
}

