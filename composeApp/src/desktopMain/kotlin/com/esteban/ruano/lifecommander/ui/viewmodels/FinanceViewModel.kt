package com.esteban.ruano.lifecommander.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esteban.ruano.lifecommander.models.finance.Budget
import com.esteban.ruano.lifecommander.models.finance.BudgetFilters
import com.esteban.ruano.lifecommander.models.finance.TransactionFilters
import com.esteban.ruano.lifecommander.services.finance.FinanceService
import com.esteban.ruano.lifecommander.ui.state.FinanceState
import com.esteban.ruano.utils.DateUIUtils.formatDefault
import com.esteban.ruano.utils.DateUIUtils.getCurrentDateTime
import com.lifecommander.finance.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

class FinanceViewModel(
    private val service: FinanceService
) : ViewModel(), FinanceActions {

    private val _state = MutableStateFlow(FinanceState())
    val state: StateFlow<FinanceState> = _state.asStateFlow()

    override fun getTransactions(refresh: Boolean) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(
                    isLoading = true,
                    error = null,
                    currentPage = if (refresh) 0 else _state.value.currentPage
                )

                val response = service.getTransactions(
                    limit = _state.value.pageSize,
                    offset = _state.value.currentPage * _state.value.pageSize,
                    filters = _state.value.transactionFilters
                )

                _state.value = _state.value.copy(
                    transactions = if (refresh) response.transactions else _state.value.transactions + response.transactions,
                    totalTransactions = response.totalCount,
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

    override fun changeTransactionFilters(filters: TransactionFilters, onSuccess: () -> Unit) {
        _state.value = _state.value.copy(
            transactionFilters = filters,
            currentPage = 0
        )
        onSuccess.invoke()
    }

    fun loadNextPage() {
        if (_state.value.transactions.size < _state.value.totalTransactions) {
            getTransactions(refresh = false)
        }
    }

    fun updateTransactionFilters(filters: TransactionFilters) {
        _state.value = _state.value.copy(
            transactionFilters = filters,
            currentPage = 0 // Reset to first page when filters change
        )
        getTransactions(refresh = true)
    }

    override fun changeBudgetFilters(filters: BudgetFilters) {
        _state.value = _state.value.copy(
            budgetFilters = filters,
            currentPage = 0
        )
        getBudgets()
    }

    override fun changeBudgetBaseDate(date: LocalDate) {
        _state.value = _state.value.copy(
            budgetBaseDate = date.formatDefault()
        )
        getBudgets()
    }

    override fun categorizeAll() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)
                service.categorizeAllTransactions(
                    referenceDate = getCurrentDateTime().date.formatDefault()
                )
                getBudgets()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    override fun categorizeUnbudgeted() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)
                service.categorizeUnbudgeted(
                    referenceDate = getCurrentDateTime().date.formatDefault()
                )
                getBudgets()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    override fun selectAccount(account: Account?) {
        _state.value = _state.value.copy(
            selectedAccount = account,
            transactionFilters = _state.value.transactionFilters.copy(
                accountIds = account?.id?.let { listOf(it) }
            )
        )
        getTransactions(refresh = true)
    }

    override fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)
                val newTransaction = service.addTransaction(transaction)
                getTransactions(refresh = true)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    override fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)
                val updatedTransaction = service.updateTransaction(transaction)
                getTransactions(refresh = true)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    override fun deleteTransaction(id: String) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)
                service.deleteTransaction(id)
                getTransactions(refresh = true)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    override fun addAccount(account: Account) {
        viewModelScope.launch{
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

    override fun updateAccount(account: Account) {
        viewModelScope.launch{
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)
                val updatedAccount = service.updateAccount(account)
                _state.value = _state.value.copy(
                    accounts = _state.value.accounts.map {
                        if (it.id == updatedAccount.id) updatedAccount else it
                    },
                    selectedAccount = if (_state.value.selectedAccount?.id == updatedAccount.id) updatedAccount else _state.value.selectedAccount,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }    }

    override fun deleteAccount(id: String) {
        viewModelScope.launch{
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)
                service.deleteAccount(id)
                _state.value = _state.value.copy(
                    accounts = _state.value.accounts.filter { it.id != id },
                    selectedAccount = if (_state.value.selectedAccount?.id == id) null else _state.value.selectedAccount,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }    }

    override fun getAccounts(){
        viewModelScope.launch{
            val response = service.getAccounts()
            _state.value = _state.value.copy(accounts = response)
        }
    }

    override fun addBudget(budget: Budget) {
        viewModelScope.launch{
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)
                val newBudget = service.addBudget(budget)
                getBudgets()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }    }

    override fun updateBudget(budget: Budget) {
        viewModelScope.launch{
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)
                val updatedBudget = service.updateBudget(budget)
               getBudgets()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }    }

    override fun deleteBudget(id: String) {
        viewModelScope.launch{
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)
                service.deleteBudget(id)
                _state.value = _state.value.copy(
                    budgets = _state.value.budgets.filter { it.budget.id != id },
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }    }

    fun updateBudgetFilters(filters: BudgetFilters) {
        _state.value = _state.value.copy(
            budgetFilters = filters
        )
        getBudgets()
    }

    override fun getBudgets() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)
                val budgets = service.getBudgetsWithProgress(
                    filters = _state.value.budgetFilters,
                    referenceDate = _state.value.budgetBaseDate
                )
                _state.value = _state.value.copy(
                    budgets = budgets,
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

    override fun getBudgetProgress(budgetId: String){
        viewModelScope.launch{ service.getBudgetProgress(budgetId) }
    }

    override fun addSavingsGoal(goal: SavingsGoal) {
        viewModelScope.launch{
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)
                val newGoal = service.addSavingsGoal(goal)
                val progress = service.getSavingsGoalProgress(newGoal.id ?: "")
                _state.value = _state.value.copy(
                    savingsGoals = _state.value.savingsGoals + newGoal,
                    savingsGoalProgress = _state.value.savingsGoalProgress + ((newGoal.id ?: "") to progress.percentageComplete),
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }    }

    override fun updateSavingsGoal(goal: SavingsGoal) {
        viewModelScope.launch{
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)
                val updatedGoal = service.updateSavingsGoal(goal)
                val progress = service.getSavingsGoalProgress(updatedGoal.id ?: "")
                _state.value = _state.value.copy(
                    savingsGoals = _state.value.savingsGoals.map {
                        if (it.id == updatedGoal.id) updatedGoal else it
                    },
                    savingsGoalProgress = _state.value.savingsGoalProgress + ((updatedGoal.id ?: "") to progress.percentageComplete),
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }    }

    override fun deleteSavingsGoal(id: String) {
        viewModelScope.launch{
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)
                service.deleteSavingsGoal(id)
                _state.value = _state.value.copy(
                    savingsGoals = _state.value.savingsGoals.filter { it.id != id },
                    savingsGoalProgress = _state.value.savingsGoalProgress - id,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }    }

    override fun getSavingsGoals() {
        viewModelScope.launch{
            val response = service.getSavingsGoals()
            _state.value = _state.value.copy(savingsGoals = response)
        }
    }

    override fun getSavingsGoalProgress(goalId: String){
        viewModelScope.launch{
            val response = service.getSavingsGoalProgress(goalId)
            _state.value = _state.value.copy(savingsGoalProgress = _state.value.savingsGoalProgress + (goalId to response.percentageComplete))
        }
    }

    fun importTransactions(text: String, accountId: String, skipDuplicates: Boolean = true) {
        viewModelScope.launch{
            try {
                _state.value = _state.value.copy(isLoading = true, error = null, transactionFilters = _state.value.transactionFilters.copy(accountIds = listOf(accountId)))
                val transactionIds = service.importTransactions(text, accountId, skipDuplicates)
                val transactions = service.getTransactions()
                _state.value = _state.value.copy(
                    transactions = transactions.transactions,
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

    fun previewTransactionImport(text: String, accountId: String): Unit {
        viewModelScope.launch {
            val response = service.previewTransactionImport(text, accountId)
            _state.value = _state.value.copy(
                importPreview =  response,
            )
        }
    }

    fun updateSearchPattern(pattern: String) {
        _state.value = _state.value.copy(
            transactionFilters = _state.value.transactionFilters.copy(
                searchPattern = pattern.takeIf { it.isNotBlank() }
            ),
            currentPage = 0
        )
        getTransactions(refresh = true)
    }

    fun updateDateRange(startDate: String?, endDate: String?) {
        _state.value = _state.value.copy(
            transactionFilters = _state.value.transactionFilters.copy(
                startDate = startDate,
                endDate = endDate
            ),
            currentPage = 0
        )
        getTransactions(refresh = true)
    }

    fun updateAmountRange(minAmount: Double?, maxAmount: Double?) {
        _state.value = _state.value.copy(
            transactionFilters = _state.value.transactionFilters.copy(
                minAmount = minAmount,
                maxAmount = maxAmount
            ),
            currentPage = 0
        )
        getTransactions(refresh = true)
    }

    fun updateTransactionTypes(types: List<TransactionType>) {
        _state.value = _state.value.copy(
            transactionFilters = _state.value.transactionFilters.copy(
                types = types.takeIf { it.isNotEmpty() }
            ),
            currentPage = 0
        )
        getTransactions(refresh = true)
    }

    fun updateCategories(categories: List<String>) {
        _state.value = _state.value.copy(
            transactionFilters = _state.value.transactionFilters.copy(
                categories = categories.takeIf { it.isNotEmpty() }
            ),
            currentPage = 0
        )
        getTransactions(refresh = true)
    }

    fun clearFilters() {
        _state.value = _state.value.copy(
            transactionFilters = TransactionFilters(),
            currentPage = 0
        )
        getTransactions(refresh = true)
    }

    override fun getBudgetTransactions(budgetId: String) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)
                val transactions = service.getBudgetTransactions(budgetId, referenceDate = getCurrentDateTime().date.formatDefault(), filters = _state.value.transactionFilters)
                _state.value = _state.value.copy(
                    transactions = transactions,
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

    override fun getScheduledTransactions(refresh: Boolean) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(
                    isLoading = true,
                    error = null,
                    currentPage = if (refresh) 0 else _state.value.currentPage
                )

                val response = service.getScheduledTransactions(
                    limit = _state.value.pageSize,
                    offset = _state.value.currentPage * _state.value.pageSize,
                    filters = _state.value.transactionFilters
                )

                _state.value = _state.value.copy(
                    scheduledTransactions = response.transactions,
                    totalScheduledTransactions = response.totalCount,
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

    override fun addScheduledTransaction(transaction: ScheduledTransaction) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)
                val newTransaction = service.addScheduledTransaction(transaction)
                getScheduledTransactions(refresh = true)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    override fun updateScheduledTransaction(transaction: ScheduledTransaction) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)
                val updatedTransaction = service.updateScheduledTransaction(transaction)
                getScheduledTransactions(refresh = true)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    override fun deleteScheduledTransaction(id: String) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)
                service.deleteScheduledTransaction(id)
                getScheduledTransactions(refresh = true)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

}