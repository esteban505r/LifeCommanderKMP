package com.esteban.ruano.lifecommander.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.esteban.ruano.lifecommander.ui.state.FinanceTab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class FinanceCoordinatorState(
    val selectedTab: FinanceTab = FinanceTab.ACCOUNTS
)

class FinanceCoordinatorViewModel : ViewModel() {
    private val _state = MutableStateFlow(FinanceCoordinatorState())
    val state: StateFlow<FinanceCoordinatorState> = _state.asStateFlow()

    fun setSelectedTab(tabIndex: FinanceTab) {
        _state.value = _state.value.copy(selectedTab = tabIndex)
    }
}

