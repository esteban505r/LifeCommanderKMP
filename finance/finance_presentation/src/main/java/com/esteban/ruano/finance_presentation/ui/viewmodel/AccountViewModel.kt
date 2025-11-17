package com.esteban.ruano.finance_presentation.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.esteban.ruano.core_ui.view_model.BaseViewModel
import com.esteban.ruano.finance_domain.use_case.FinanceUseCases
import com.esteban.ruano.finance_presentation.ui.intent.AccountIntent
import com.esteban.ruano.finance_presentation.ui.intent.FinanceEffect
import com.esteban.ruano.finance_presentation.ui.viewmodel.state.AccountState
import com.lifecommander.finance.model.Account
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val financeUseCases: FinanceUseCases
) : BaseViewModel<AccountIntent, AccountState, FinanceEffect>() {
    
    override fun createInitialState(): AccountState {
        return AccountState()
    }

    override fun handleIntent(intent: AccountIntent) {
        when (intent) {
            is AccountIntent.GetAccounts -> getAccounts()
            is AccountIntent.AddAccount -> addAccount(intent.account)
            is AccountIntent.UpdateAccount -> updateAccount(intent.account)
            is AccountIntent.DeleteAccount -> deleteAccount(intent.id)
            is AccountIntent.SelectAccount -> selectAccount(intent.account)
        }
    }

    fun getAccounts() {
        viewModelScope.launch {
            financeUseCases.getAccounts().fold(
                onSuccess = { accounts ->
                    emitState { currentState.copy(accounts = accounts, error = null) }
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

    fun addAccount(account: Account) {
        viewModelScope.launch {
            emitState { currentState.copy(isLoading = true, error = null) }
            
            financeUseCases.addAccount(account).fold(
                onSuccess = { newAccount ->
                    emitState {
                        currentState.copy(
                            accounts = currentState.accounts + newAccount,
                            isLoading = false
                        )
                    }
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

    fun updateAccount(account: Account) {
        viewModelScope.launch {
            emitState { currentState.copy(isLoading = true, error = null) }
            
            financeUseCases.updateAccount(account).fold(
                onSuccess = { updatedAccount ->
                    emitState {
                        currentState.copy(
                            accounts = currentState.accounts.map {
                                if (it.id == updatedAccount.id) updatedAccount else it
                            },
                            selectedAccount = if (currentState.selectedAccount?.id == updatedAccount.id) 
                                updatedAccount 
                            else 
                                currentState.selectedAccount,
                            isLoading = false
                        )
                    }
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

    fun deleteAccount(id: String) {
        viewModelScope.launch {
            emitState { currentState.copy(isLoading = true, error = null) }
            
            financeUseCases.deleteAccount(id).fold(
                onSuccess = {
                    emitState {
                        currentState.copy(
                            accounts = currentState.accounts.filter { it.id != id },
                            selectedAccount = if (currentState.selectedAccount?.id == id) 
                                null 
                            else 
                                currentState.selectedAccount,
                            isLoading = false
                        )
                    }
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

    fun selectAccount(account: Account?) {
        emitState {
            currentState.copy(selectedAccount = account)
        }
    }
}

