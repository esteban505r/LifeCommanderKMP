package com.esteban.ruano.finance_presentation.ui.intent

import com.esteban.ruano.core_ui.view_model.UserIntent
import com.esteban.ruano.lifecommander.ui.state.FinanceTab

sealed class FinanceCoordinatorIntent : UserIntent {
    data class ChangeTab(val tab: FinanceTab) : FinanceCoordinatorIntent()
}

