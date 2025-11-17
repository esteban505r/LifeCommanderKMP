package com.esteban.ruano.lifecommander.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esteban.ruano.lifecommander.models.finance.TransactionFilters
import com.esteban.ruano.lifecommander.services.finance.FinanceService
import com.esteban.ruano.lifecommander.utils.PaginatedDataFetcher
import com.lifecommander.finance.model.Transaction
import com.lifecommander.finance.model.TransactionImportPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TransactionState(
    val transactions: List<Transaction> = emptyList(),
    val totalTransactions: Long = 0,
    val transactionFilters: TransactionFilters = TransactionFilters(),
    val currentPage: Int = 0,
    val pageSize: Int = 20,
    val isLoading: Boolean = false,
    val isLoadingTransactions: Boolean = false,
    val error: String? = null,
    val importPreview: TransactionImportPreview? = null,
    val currentBudgetId: String? = null
)

class TransactionViewModel(
    private val service: FinanceService
) : ViewModel() {

    private val _state = MutableStateFlow(TransactionState())
    val state: StateFlow<TransactionState> = _state.asStateFlow()

    private val transactionsFetcher = PaginatedDataFetcher(
        pageSize = 20,
        fetchData = { page, pageSize ->
            service.getTransactions(pageSize, page).transactions
        }
    )

    private val budgetTransactionsFetcher = PaginatedDataFetcher(
        pageSize = 20,
        fetchData = { page, pageSize ->
            service.getBudgetTransactions(
                budgetId = _state.value.currentBudgetId!!,
                filters = _state.value.transactionFilters,
                limit = pageSize,
                offset = page
            )
        }
    )

    init {
        viewModelScope.launch {
            transactionsFetcher.data.collect { stateValue ->
                _state.value = _state.value.copy(
                    transactions = stateValue,
                    isLoadingTransactions = false
                )
            }
        }

        viewModelScope.launch {
            budgetTransactionsFetcher.data.collect { budgetTransactions ->
                _state.value = _state.value.copy(
                    transactions = budgetTransactions,
                    isLoadingTransactions = false
                )
            }
        }
    }

    fun getTransactions(refresh: Boolean) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(
                    currentBudgetId = null,
                    isLoadingTransactions = true,
                    error = null,
                    currentPage = if (refresh) 0 else _state.value.currentPage
                )

                transactionsFetcher.loadNextPage(reset = refresh)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoadingTransactions = false
                )
            }
        }
    }

    fun getBudgetTransactions(budgetId: String, refresh: Boolean = false) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(
                    isLoadingTransactions = true, 
                    error = null, 
                    currentBudgetId = budgetId
                )

                budgetTransactionsFetcher.loadNextPage(reset = refresh)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoadingTransactions = false
                )
            }
        }
    }

    fun changeTransactionFilters(filters: TransactionFilters, onSuccess: () -> Unit) {
        _state.value = _state.value.copy(
            transactionFilters = filters,
            currentPage = 0
        )
        // Reset will be handled by loadNextPage(reset = true) when onSuccess calls getTransactions
        onSuccess.invoke()
    }

    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoadingTransactions = true, error = null)
                service.addTransaction(transaction)
                if (_state.value.currentBudgetId != null) {
                    getBudgetTransactions(_state.value.currentBudgetId!!, refresh = true)
                } else {
                    getTransactions(refresh = true)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoadingTransactions = false
                )
            }
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoadingTransactions = true, error = null)
                service.updateTransaction(transaction)
                if (_state.value.currentBudgetId != null) {
                    getBudgetTransactions(_state.value.currentBudgetId!!, refresh = true)
                } else {
                    getTransactions(refresh = true)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoadingTransactions = false
                )
            }
        }
    }

    fun deleteTransaction(id: String) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoadingTransactions = true, error = null)
                service.deleteTransaction(id)
                if (_state.value.currentBudgetId != null) {
                    getBudgetTransactions(_state.value.currentBudgetId!!, refresh = true)
                } else {
                    getTransactions(refresh = true)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoadingTransactions = false
                )
            }
        }
    }

    fun importTransactions(text: String, accountId: String, skipDuplicates: Boolean = true) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(
                    isLoadingTransactions = true, 
                    error = null, 
                    transactionFilters = _state.value.transactionFilters.copy(accountIds = listOf(accountId))
                )
                service.importTransactions(text, accountId, skipDuplicates)
                val transactions = service.getTransactions()
                _state.value = _state.value.copy(
                    transactions = transactions.transactions,
                    isLoadingTransactions = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoadingTransactions = false
                )
            }
        }
    }

    fun previewTransactionImport(text: String, accountId: String) {
        viewModelScope.launch {
            try {
                val response = service.previewTransactionImport(text, accountId)
                _state.value = _state.value.copy(
                    importPreview = response,
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message
                )
            }
        }
    }

    fun loadNextPage() {
        getTransactions(refresh = false)
    }

    fun setCurrentBudgetId(budgetId: String?) {
        _state.value = _state.value.copy(currentBudgetId = budgetId)
    }
}

