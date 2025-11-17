package com.esteban.ruano.finance_presentation.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.esteban.ruano.core_ui.view_model.BaseViewModel
import com.esteban.ruano.finance_domain.use_case.FinanceUseCases
import com.esteban.ruano.finance_presentation.ui.intent.FinanceEffect
import com.esteban.ruano.finance_presentation.ui.intent.TransactionIntent
import com.esteban.ruano.finance_presentation.ui.viewmodel.state.TransactionState
import com.esteban.ruano.lifecommander.models.finance.TransactionFilters
import com.lifecommander.finance.model.Transaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val financeUseCases: FinanceUseCases
) : BaseViewModel<TransactionIntent, TransactionState, FinanceEffect>() {
    
    override fun createInitialState(): TransactionState {
        return TransactionState()
    }

    override fun handleIntent(intent: TransactionIntent) {
        when (intent) {
            is TransactionIntent.GetTransactions -> getTransactions(intent.refresh)
            is TransactionIntent.AddTransaction -> addTransaction(intent.transaction)
            is TransactionIntent.UpdateTransaction -> updateTransaction(intent.transaction)
            is TransactionIntent.DeleteTransaction -> deleteTransaction(intent.id)
            is TransactionIntent.ChangeTransactionFilters -> changeTransactionFilters(intent.filters) {}
            is TransactionIntent.ImportTransactions -> importTransactions(intent.text, intent.accountId, intent.skipDuplicates)
            is TransactionIntent.PreviewTransactionImport -> previewTransactionImport(intent.text, intent.accountId)
        }
    }

    fun getTransactions(refresh: Boolean) {
        viewModelScope.launch {
            emitState {
                currentState.copy(
                    isLoadingTransactions = true,
                    error = null,
                    currentPage = if (refresh) 0 else currentState.currentPage
                )
            }

            financeUseCases.getTransactions(
                filter = null,
                page = currentState.currentPage,
                limit = currentState.pageSize,
                filters = currentState.transactionFilters
            ).fold(
                onSuccess = { response ->
                    emitState {
                        currentState.copy(
                            transactions = if (refresh) response.transactions else currentState.transactions + response.transactions,
                            totalTransactions = response.totalCount,
                            isLoadingTransactions = false
                        )
                    }
                },
                onFailure = { error ->
                    emitState {
                        currentState.copy(
                            error = error.message,
                            isLoadingTransactions = false
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

    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            emitState { currentState.copy(isLoading = true, error = null) }
            financeUseCases.addTransaction(transaction).fold(
                onSuccess = {
                    getTransactions(refresh = true)
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

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            emitState { currentState.copy(isLoading = true, error = null) }
            financeUseCases.updateTransaction(transaction).fold(
                onSuccess = {
                    getTransactions(refresh = true)
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

    fun deleteTransaction(id: String) {
        viewModelScope.launch {
            emitState { currentState.copy(isLoading = true, error = null) }
            financeUseCases.deleteTransaction(id).fold(
                onSuccess = {
                    getTransactions(refresh = true)
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

    fun importTransactions(text: String, accountId: String, skipDuplicates: Boolean) {
        viewModelScope.launch {
            try {
                emitState { 
                    currentState.copy(
                        isLoading = true, 
                        error = null,
                        transactionFilters = currentState.transactionFilters.copy(
                            accountIds = listOf(accountId)
                        )
                    )
                }
                
                financeUseCases.importTransactions(text, accountId, skipDuplicates).fold(
                    onSuccess = {
                        getTransactions(refresh = true)
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

    fun previewTransactionImport(text: String, accountId: String) {
        viewModelScope.launch {
            financeUseCases.previewTransactionImport(text, accountId).fold(
                onSuccess = { preview ->
                    emitState {
                        currentState.copy(
                            importPreview = preview
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
}

