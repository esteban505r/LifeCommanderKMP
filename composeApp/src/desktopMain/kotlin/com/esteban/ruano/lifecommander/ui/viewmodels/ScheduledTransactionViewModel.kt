package com.esteban.ruano.lifecommander.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esteban.ruano.lifecommander.models.finance.TransactionFilters
import com.esteban.ruano.lifecommander.services.finance.FinanceService
import com.esteban.ruano.lifecommander.utils.PaginatedDataFetcher
import com.lifecommander.finance.model.ScheduledTransaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ScheduledTransactionState(
    val scheduledTransactions: List<ScheduledTransaction> = emptyList(),
    val totalScheduledTransactions: Long = 0,
    val transactionFilters: TransactionFilters = TransactionFilters(),
    val currentPage: Int = 0,
    val pageSize: Int = 20,
    val isLoading: Boolean = false,
    val error: String? = null
)

class ScheduledTransactionViewModel(
    private val service: FinanceService
) : ViewModel() {

    private val _state = MutableStateFlow(ScheduledTransactionState())
    val state: StateFlow<ScheduledTransactionState> = _state.asStateFlow()

    private val scheduledTransactionsFetcher = PaginatedDataFetcher(
        pageSize = 20,
        fetchData = { page, pageSize ->
            service.getScheduledTransactions(
                filters = _state.value.transactionFilters,
                limit = pageSize,
                offset = page
            ).transactions
        }
    )

    init {
        viewModelScope.launch {
            scheduledTransactionsFetcher.data.collect { scheduledTransactions ->
                _state.value = _state.value.copy(
                    scheduledTransactions = scheduledTransactions,
                    isLoading = false
                )
            }
        }
    }

    fun getScheduledTransactions(refresh: Boolean) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)

                scheduledTransactionsFetcher.loadNextPage(reset = refresh)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun addScheduledTransaction(transaction: ScheduledTransaction) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)
                service.addScheduledTransaction(transaction)
                getScheduledTransactions(refresh = true)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun updateScheduledTransaction(transaction: ScheduledTransaction) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)
                service.updateScheduledTransaction(transaction)
                getScheduledTransactions(refresh = true)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun deleteScheduledTransaction(id: String) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)
                service.deleteScheduledTransaction(id)
                getScheduledTransactions(refresh = true)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun changeTransactionFilters(filters: TransactionFilters, onSuccess: () -> Unit) {
        _state.value = _state.value.copy(
            transactionFilters = filters,
            currentPage = 0
        )
        // Reset will be handled by loadNextPage(reset = true) when onSuccess calls getScheduledTransactions
        onSuccess.invoke()
    }
}

