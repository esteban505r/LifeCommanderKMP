package com.esteban.ruano.finance_presentation.ui.viewmodel

import com.esteban.ruano.core_ui.view_model.BaseViewModel
import com.esteban.ruano.finance_presentation.ui.intent.FinanceCoordinatorIntent
import com.esteban.ruano.finance_presentation.ui.intent.FinanceEffect
import com.esteban.ruano.finance_presentation.ui.viewmodel.state.FinanceCoordinatorState
import com.esteban.ruano.lifecommander.ui.state.FinanceTab
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FinanceCoordinatorViewModel @Inject constructor() : BaseViewModel<FinanceCoordinatorIntent, FinanceCoordinatorState, FinanceEffect>() {
    
    override fun createInitialState(): FinanceCoordinatorState {
        return FinanceCoordinatorState()
    }

    override fun handleIntent(intent: FinanceCoordinatorIntent) {
        when (intent) {
            is FinanceCoordinatorIntent.ChangeTab -> setSelectedTab(intent.tab)
        }
    }

    fun setSelectedTab(tabIndex: FinanceTab) {
        emitState {
            currentState.copy(
                selectedTab = tabIndex
            )
        }
    }
}

