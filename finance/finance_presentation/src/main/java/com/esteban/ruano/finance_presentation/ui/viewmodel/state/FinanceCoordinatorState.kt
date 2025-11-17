package com.esteban.ruano.finance_presentation.ui.viewmodel.state

import com.esteban.ruano.core_ui.view_model.ViewState
import com.esteban.ruano.lifecommander.ui.state.FinanceTab

data class FinanceCoordinatorState(
    val selectedTab: FinanceTab = FinanceTab.ACCOUNTS,
    val isLoading: Boolean = false,
    val error: String? = null
) : ViewState

