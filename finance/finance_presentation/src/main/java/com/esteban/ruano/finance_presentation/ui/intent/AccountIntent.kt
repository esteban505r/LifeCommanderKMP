package com.esteban.ruano.finance_presentation.ui.intent

import com.esteban.ruano.core_ui.view_model.UserIntent
import com.lifecommander.finance.model.Account

sealed class AccountIntent : UserIntent {
    object GetAccounts : AccountIntent()
    data class AddAccount(val account: Account) : AccountIntent()
    data class UpdateAccount(val account: Account) : AccountIntent()
    data class DeleteAccount(val id: String) : AccountIntent()
    data class SelectAccount(val account: Account?) : AccountIntent()
}

