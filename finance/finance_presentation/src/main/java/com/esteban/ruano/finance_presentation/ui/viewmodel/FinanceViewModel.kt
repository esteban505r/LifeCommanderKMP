package com.esteban.ruano.finance_presentation.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.esteban.ruano.core_ui.view_model.BaseViewModel
import com.esteban.ruano.finance_domain.use_case.FinanceUseCases
import com.esteban.ruano.finance_presentation.ui.intent.FinanceEffect
import com.esteban.ruano.finance_presentation.ui.intent.FinanceIntent
import com.esteban.ruano.finance_presentation.ui.viewmodel.state.FinanceState
import com.esteban.ruano.lifecommander.models.finance.Budget
import com.esteban.ruano.lifecommander.models.finance.BudgetFilters
import com.esteban.ruano.lifecommander.models.finance.TransactionFilters
import com.esteban.ruano.lifecommander.ui.state.FinanceTab
import com.esteban.ruano.utils.DateUIUtils.formatDefault
import com.esteban.ruano.utils.DateUIUtils.getCurrentDateTime
import com.lifecommander.finance.model.Account
import com.lifecommander.finance.model.FinanceActions
import com.lifecommander.finance.model.SavingsGoal
import com.lifecommander.finance.model.ScheduledTransaction
import com.lifecommander.finance.model.Transaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toKotlinLocalDate
import javax.inject.Inject

@HiltViewModel
class FinanceViewModel @Inject constructor(
    private val financeUseCases: FinanceUseCases
) : BaseViewModel<FinanceIntent, FinanceState, FinanceEffect>(), FinanceActions {
    override fun createInitialState(): FinanceState {
        return FinanceState()
    }

    override fun handleIntent(intent: FinanceIntent) {
        when (intent) {
            // Transaction Intents
            is FinanceIntent.GetTransactions -> getTransactions(intent.refresh)
            is FinanceIntent.AddTransaction -> addTransaction(intent.transaction)
            is FinanceIntent.UpdateTransaction -> updateTransaction(intent.transaction)
            is FinanceIntent.DeleteTransaction -> deleteTransaction(intent.id)
            is FinanceIntent.ChangeTransactionFilters -> changeTransactionFilters(intent.filters){}
            
            // Account Intents
            is FinanceIntent.GetAccounts -> getAccounts()
            is FinanceIntent.AddAccount -> addAccount(intent.account)
            is FinanceIntent.UpdateAccount -> updateAccount(intent.account)
            is FinanceIntent.DeleteAccount -> deleteAccount(intent.id)
            is FinanceIntent.SelectAccount -> selectAccount(intent.account)
            
            // Budget Intents
            is FinanceIntent.GetBudgets -> getBudgets()
            is FinanceIntent.AddBudget -> addBudget(intent.budget)
            is FinanceIntent.UpdateBudget -> updateBudget(intent.budget)
            is FinanceIntent.DeleteBudget -> deleteBudget(intent.id)
            is FinanceIntent.ChangeBudgetFilters -> changeBudgetFilters(intent.filters)
            is FinanceIntent.ChangeBudgetBaseDate -> changeBudgetBaseDate(intent.date.toKotlinLocalDate())
            
            // Savings Goal Intents
            is FinanceIntent.GetSavingsGoals -> getSavingsGoals()
            is FinanceIntent.AddSavingsGoal -> addSavingsGoal(intent.goal)
            is FinanceIntent.UpdateSavingsGoal -> updateSavingsGoal(intent.goal)
            is FinanceIntent.DeleteSavingsGoal -> deleteSavingsGoal(intent.id)
            
            // Scheduled Transaction Intents
            is FinanceIntent.GetScheduledTransactions -> getScheduledTransactions(intent.refresh)
            is FinanceIntent.AddScheduledTransaction -> addScheduledTransaction(intent.transaction)
            is FinanceIntent.UpdateScheduledTransaction -> updateScheduledTransaction(intent.transaction)
            is FinanceIntent.DeleteScheduledTransaction -> deleteScheduledTransaction(intent.id)
            
            // Utility Intents
            is FinanceIntent.CategorizeAllTransactions -> categorizeAll()
            is FinanceIntent.CategorizeUnbudgeted -> categorizeUnbudgeted()
            is FinanceIntent.ImportTransactions -> importTransactions(intent.text, intent.accountId, intent.skipDuplicates)
            is FinanceIntent.PreviewTransactionImport -> previewTransactionImport(intent.text, intent.accountId)
            is FinanceIntent.ChangeTab -> TODO()
        }
    }

    // Transaction functions
    override fun getTransactions(refresh: Boolean) {
        viewModelScope.launch {
            emitState {
                currentState.copy(
                    isLoading = true,
                    error = null,
                    currentPage = if (refresh) 0 else currentState.currentPage
                )
            }

            financeUseCases.getTransactions(
                filter = null,
                page = currentState.currentPage,
                limit = currentState.pageSize,
                filters = currentState.transactionFilters
            ).fold(
                onSuccess = { response ->
                    emitState {
                        currentState.copy(
                            transactions = if (refresh) response.transactions else currentState.transactions + response.transactions,
                            totalTransactions = response.totalCount,
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

    override fun changeTransactionFilters(filters: TransactionFilters, onSuccess: () -> Unit) {
        emitState {
            currentState.copy(
                transactionFilters = filters,
                currentPage = 0
            )
        }
        onSuccess.invoke()
    }

    // Account functions
    override fun getAccounts() {
        viewModelScope.launch {
            financeUseCases.getAccounts().fold(
                onSuccess = { accounts ->
                    emitState { currentState.copy(accounts = accounts) }
                },
                onFailure = { error ->
                    emitState {
                        currentState.copy(
                            error = error.message
                        )
                    }
                }
            )
        }
    }

    override fun setSelectedTab(tabIndex: FinanceTab) {
        emitState {
            currentState.copy(
                selectedTab = tabIndex
            )
        }
    }

    override fun addAccount(account: Account) {
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

    override fun updateAccount(account: Account) {
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

    override fun deleteAccount(id: String) {
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

    override fun selectAccount(account: Account?) {
        emitState {
            currentState.copy(
                selectedAccount = account,
                transactionFilters = currentState.transactionFilters.copy(
                    accountIds = account?.id?.let { listOf(it) }
                )
            )
        }
        getTransactions(refresh = true)
    }

    // Budget functions
    override fun getBudgets() {
        viewModelScope.launch {
            emitState { currentState.copy(isLoading = true, error = null) }
            
            financeUseCases.getBudgets(
                filters = currentState.budgetFilters,
                referenceDate = currentState.budgetBaseDate.ifEmpty { 
                    getCurrentDateTime(
                        TimeZone.currentSystemDefault()
                    ).date.formatDefault()
                }
            ).fold(
                onSuccess = { budgets ->
                    emitState {
                        currentState.copy(
                            budgets = budgets,
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

    override fun addBudget(budget: Budget) {
        viewModelScope.launch {
            emitState { currentState.copy(isLoading = true, error = null) }
            
            financeUseCases.addBudget(budget).fold(
                onSuccess = {
                    getBudgets()
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

    override fun updateBudget(budget: Budget) {
        viewModelScope.launch {
            emitState { currentState.copy(isLoading = true, error = null) }
            
            financeUseCases.updateBudget(budget).fold(
                onSuccess = {
                    getBudgets()
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

    override fun deleteBudget(id: String) {
        viewModelScope.launch {
            emitState { currentState.copy(isLoading = true, error = null) }
            
            financeUseCases.deleteBudget(id).fold(
                onSuccess = {
                    emitState {
                        currentState.copy(
                            budgets = currentState.budgets.filter { it.budget.id != id },
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

    override fun changeBudgetFilters(filters: BudgetFilters) {
        emitState {
            currentState.copy(
                budgetFilters = filters
            )
        }
        getBudgets()
    }

    override fun changeBudgetBaseDate(date: kotlinx.datetime.LocalDate) {
        emitState {
            currentState.copy(
                budgetBaseDate = date.formatDefault()
            )
        }
        getBudgets()
    }

    // Savings Goal functions
    override fun getSavingsGoals() {
        viewModelScope.launch {
            financeUseCases.getSavingsGoals().fold(
                onSuccess = { goals ->
                    emitState { currentState.copy(savingsGoals = goals) }
                },
                onFailure = { error ->
                    emitState {
                        currentState.copy(
                            error = error.message
                        )
                    }
                }
            )
        }
    }

    override fun addSavingsGoal(goal: SavingsGoal) {
        viewModelScope.launch {
            emitState { currentState.copy(isLoading = true, error = null) }
            
            financeUseCases.addSavingsGoal(goal).fold(
                onSuccess = { newGoal ->
                    financeUseCases.getSavingsGoalProgress(newGoal.id ?: "").fold(
                        onSuccess = { progress ->
                            emitState {
                                currentState.copy(
                                    savingsGoals = currentState.savingsGoals + newGoal,
                                    savingsGoalProgress = currentState.savingsGoalProgress + 
                                        ((newGoal.id ?: "") to progress.percentageComplete),
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

    override fun updateSavingsGoal(goal: SavingsGoal) {
        viewModelScope.launch {
            emitState { currentState.copy(isLoading = true, error = null) }
            
            financeUseCases.updateSavingsGoal(goal).fold(
                onSuccess = { updatedGoal ->
                    financeUseCases.getSavingsGoalProgress(updatedGoal.id ?: "").fold(
                        onSuccess = { progress ->
                            emitState {
                                currentState.copy(
                                    savingsGoals = currentState.savingsGoals.map {
                                        if (it.id == updatedGoal.id) updatedGoal else it
                                    },
                                    savingsGoalProgress = currentState.savingsGoalProgress + 
                                        ((updatedGoal.id ?: "") to progress.percentageComplete),
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

    override fun deleteSavingsGoal(id: String) {
        viewModelScope.launch {
            emitState { currentState.copy(isLoading = true, error = null) }
            
            financeUseCases.deleteSavingsGoal(id).fold(
                onSuccess = {
                    emitState {
                        currentState.copy(
                            savingsGoals = currentState.savingsGoals.filter { it.id != id },
                            savingsGoalProgress = currentState.savingsGoalProgress - id,
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

    // Scheduled Transaction functions
    override fun getScheduledTransactions(refresh: Boolean) {
        viewModelScope.launch {
            emitState {
                currentState.copy(
                    isLoading = true,
                    error = null,
                    currentPage = if (refresh) 0 else currentState.currentPage
                )
            }

            financeUseCases.getScheduledTransactions(
                filter = null,
                page = currentState.currentPage,
                limit = currentState.pageSize,
                filters = currentState.transactionFilters
            ).fold(
                onSuccess = { response ->
                    emitState {
                        currentState.copy(
                            scheduledTransactions = response.transactions,
                            totalScheduledTransactions = response.totalCount,
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

    override fun addScheduledTransaction(transaction: ScheduledTransaction) {
        viewModelScope.launch {
            emitState { currentState.copy(isLoading = true, error = null) }
            
            financeUseCases.addScheduledTransaction(transaction).fold(
                onSuccess = {
                    getScheduledTransactions(refresh = true)
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

    override fun updateScheduledTransaction(transaction: ScheduledTransaction) {
        viewModelScope.launch {
            emitState { currentState.copy(isLoading = true, error = null) }
            
            financeUseCases.updateScheduledTransaction(transaction).fold(
                onSuccess = {
                    getScheduledTransactions(refresh = true)
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

    override fun deleteScheduledTransaction(id: String) {
        viewModelScope.launch {
            emitState { currentState.copy(isLoading = true, error = null) }
            
            financeUseCases.deleteScheduledTransaction(id).fold(
                onSuccess = {
                    getScheduledTransactions(refresh = true)
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

    // Utility functions
    override fun categorizeAll() {
        viewModelScope.launch {
            try {
                emitState { currentState.copy(isLoading = true, error = null) }
                financeUseCases.categorizeAllTransactions(
                    referenceDate = getCurrentDateTime(
                        TimeZone.currentSystemDefault()
                    ).date.formatDefault()
                ).fold(
                    onSuccess = {
                        getBudgets()
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
            } catch (e: Exception) {
                emitState {
                    currentState.copy(
                        error = e.message,
                        isLoading = false
                    )
                }
            }
        }
    }

    override fun categorizeUnbudgeted() {
        viewModelScope.launch {
            try {
                emitState { currentState.copy(isLoading = true, error = null) }
                financeUseCases.categorizeUnbudgeted(
                    referenceDate = getCurrentDateTime(
                        TimeZone.currentSystemDefault()
                    ).date.formatDefault()
                ).fold(
                    onSuccess = {
                        getBudgets()
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
            } catch (e: Exception) {
                emitState {
                    currentState.copy(
                        error = e.message,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun importTransactions(text: String, accountId: String, skipDuplicates: Boolean) {
        viewModelScope.launch {
            try {
                emitState { 
                    currentState.copy(
                        isLoading = true, 
                        error = null,
                        transactionFilters = currentState.transactionFilters.copy(
                            accountIds = listOf(accountId)
                        )
                    )
                }
                
                financeUseCases.importTransactions(text, accountId, skipDuplicates).fold(
                    onSuccess = {
                        getTransactions(refresh = true)
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
            } catch (e: Exception) {
                emitState {
                    currentState.copy(
                        error = e.message,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun previewTransactionImport(text: String, accountId: String) {
        viewModelScope.launch {
            financeUseCases.previewTransactionImport(text, accountId).fold(
                onSuccess = { preview ->
                    emitState {
                        currentState.copy(
                            importPreview = preview
                        )
                    }
                },
                onFailure = { error ->
                    emitState {
                        currentState.copy(
                            error = error.message
                        )
                    }
                }
            )
        }
    }

    override fun getBudgetProgress(budgetId: String) {
        viewModelScope.launch {
            financeUseCases.getBudgetProgress(budgetId).fold(
                onSuccess = { progress ->
                    emitState {
                        currentState.copy(
                            budgets = currentState.budgets.map {
                                if (it.budget.id == budgetId) {
                                    it
                                } else {
                                    it
                                }
                            }
                        )
                    }
                },
                onFailure = { error ->
                    emitState {
                        currentState.copy(
                            error = error.message
                        )
                    }
                }
            )
        }
    }

    override fun getBudgetTransactions(budgetId: String) {
        viewModelScope.launch {
            try {
                emitState { currentState.copy(isLoading = true, error = null) }
                financeUseCases.getBudgetTransactions(
                    budgetId = budgetId,
                    referenceDate = getCurrentDateTime(
                        TimeZone.currentSystemDefault()
                    ).date.formatDefault(),
                    filters = currentState.transactionFilters
                ).fold(
                    onSuccess = { transactions ->
                        emitState {
                            currentState.copy(
                                transactions = transactions,
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
            } catch (e: Exception) {
                emitState {
                    currentState.copy(
                        error = e.message,
                        isLoading = false
                    )
                }
            }
        }
    }

    override fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            emitState { currentState.copy(isLoading = true, error = null) }
            financeUseCases.addTransaction(transaction).fold(
                onSuccess = {
                    getTransactions(refresh = true)
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

    override fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            emitState { currentState.copy(isLoading = true, error = null) }
            financeUseCases.updateTransaction(transaction).fold(
                onSuccess = {
                    getTransactions(refresh = true)
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

    override fun deleteTransaction(id: String) {
        viewModelScope.launch {
            emitState { currentState.copy(isLoading = true, error = null) }
            financeUseCases.deleteTransaction(id).fold(
                onSuccess = {
                    getTransactions(refresh = true)
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

    override fun getSavingsGoalProgress(goalId: String) {
        viewModelScope.launch {
            financeUseCases.getSavingsGoalProgress(goalId).fold(
                onSuccess = { progress ->
                    emitState {
                        currentState.copy(
                            savingsGoalProgress = currentState.savingsGoalProgress + (goalId to progress.percentageComplete)
                        )
                    }
                },
                onFailure = { error ->
                    emitState {
                        currentState.copy(
                            error = error.message
                        )
                    }
                }
            )
        }
    }
} 