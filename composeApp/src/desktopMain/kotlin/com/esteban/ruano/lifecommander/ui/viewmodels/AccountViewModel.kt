package com.esteban.ruano.lifecommander.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esteban.ruano.lifecommander.services.finance.FinanceService
import com.lifecommander.finance.model.Account
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AccountState(
    val accounts: List<Account> = emptyList(),
    val selectedAccount: Account? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class AccountViewModel(
    private val service: FinanceService
) : ViewModel() {

    private val _state = MutableStateFlow(AccountState())
    val state: StateFlow<AccountState> = _state.asStateFlow()

    fun getAccounts() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)
                val response = service.getAccounts()
                _state.value = _state.value.copy(
                    accounts = response,
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

    fun addAccount(account: Account) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)
                val newAccount = service.addAccount(account)
                _state.value = _state.value.copy(
                    accounts = _state.value.accounts + newAccount,
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

    fun updateAccount(account: Account) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)
                val updatedAccount = service.updateAccount(account)
                _state.value = _state.value.copy(
                    accounts = _state.value.accounts.map {
                        if (it.id == updatedAccount.id) updatedAccount else it
                    },
                    selectedAccount = if (_state.value.selectedAccount?.id == updatedAccount.id) 
                        updatedAccount 
                    else 
                        _state.value.selectedAccount,
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

    fun deleteAccount(id: String) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)
                service.deleteAccount(id)
                _state.value = _state.value.copy(
                    accounts = _state.value.accounts.filter { it.id != id },
                    selectedAccount = if (_state.value.selectedAccount?.id == id) 
                        null 
                    else 
                        _state.value.selectedAccount,
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

    fun selectAccount(account: Account?) {
        _state.value = _state.value.copy(selectedAccount = account)
    }
}

