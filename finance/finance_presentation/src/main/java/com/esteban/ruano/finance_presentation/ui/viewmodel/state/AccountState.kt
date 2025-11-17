package com.esteban.ruano.finance_presentation.ui.viewmodel.state

import com.esteban.ruano.core_ui.view_model.ViewState
import com.lifecommander.finance.model.Account

data class AccountState(
    val accounts: List<Account> = emptyList(),
    val selectedAccount: Account? = null,
    val isLoading: Boolean = false,
    val error: String? = null
) : ViewState

