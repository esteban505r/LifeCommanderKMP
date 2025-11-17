package com.esteban.ruano.lifecommander.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esteban.ruano.lifecommander.models.FinanceStatisticsDTO
import com.esteban.ruano.lifecommander.services.finance.FinanceService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FinanceStatisticsState(
    val statistics: FinanceStatisticsDTO? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class FinanceStatisticsViewModel(
    private val service: FinanceService
) : ViewModel() {

    private val _state = MutableStateFlow(FinanceStatisticsState())
    val state: StateFlow<FinanceStatisticsState> = _state.asStateFlow()

    fun getStatistics() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)
                val statistics = service.getFinanceStatistics()
                _state.value = _state.value.copy(
                    statistics = statistics,
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
}

