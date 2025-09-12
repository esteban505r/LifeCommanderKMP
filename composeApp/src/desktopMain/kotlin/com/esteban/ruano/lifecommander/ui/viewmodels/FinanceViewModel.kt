package com.esteban.ruano.lifecommander.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esteban.ruano.lifecommander.models.finance.Budget
import com.esteban.ruano.lifecommander.models.finance.BudgetFilters
import com.esteban.ruano.lifecommander.models.finance.TransactionFilters
import com.esteban.ruano.lifecommander.services.finance.FinanceService
import com.esteban.ruano.lifecommander.ui.state.FinanceState
import com.esteban.ruano.lifecommander.ui.state.FinanceTab
import com.esteban.ruano.lifecommander.utils.PaginatedDataFetcher
import com.esteban.ruano.utils.DateUIUtils.formatDefault
import com.esteban.ruano.utils.DateUIUtils.getCurrentDateTime
import com.lifecommander.finance.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

class FinanceViewModel(
    private val service: FinanceService
) : ViewModel(), FinanceActions {

    private val _state = MutableStateFlow(FinanceState())
    val state: StateFlow<FinanceState> = _state.asStateFlow()

    private val transactionsFetcher = PaginatedDataFetcher(
        pageSize = 20,
        fetchData = { page, pageSize ->
            service.getTransactions( pageSize,page).transactions
        }
    )

    private val budgetsFetcher = PaginatedDataFetcher(
        pageSize = 20,
        fetchData = { page, pageSize ->
            service.getBudgetsWithProgress(
                filters = _state.value.budgetFilters,
                referenceDate = _state.value.budgetBaseDate,
                limit = pageSize,
                offset = page
            )
        }
    )

    private val scheduledTransactionsFetcher = PaginatedDataFetcher(
        pageSize = 20,
        fetchData = { page, pageSize ->
            service.getScheduledTransactions(
                filters = _state.value.transactionFilters,
                limit = pageSize,
                offset = page
            ).transactions
        }
    )

    private val budgetTransactionsFetcher = PaginatedDataFetcher(
        pageSize = 20,
        fetchData = { page, pageSize ->
            service.getBudgetTransactions(
                budgetId = _state.value.currentBudgetId!!,
                filters = _state.value.transactionFilters,
                limit = pageSize,
                offset = page,
                referenceDate = _state.value.budgetBaseDate,
            )
        }
    )



    init{
        viewModelScope.launch {
            transactionsFetcher.data.collect{ stateValue ->
                _state.value = _state.value.copy(
                    transactions = stateValue,
                    isLoadingTransactions = false
                )
            }
        }

        // Collecting paged data for budgets
        viewModelScope.launch {
            budgetsFetcher.data.collect { budgets ->
                _state.value = _state.value.copy(
                    budgets = budgets,
                    isLoadingBudgets = false
                )
            }
        }

        // Collecting paged data for scheduled transactions
        viewModelScope.launch {
            scheduledTransactionsFetcher.data.collect { scheduledTransactions ->
                _state.value = _state.value.copy(
                    scheduledTransactions = scheduledTransactions,
                    isLoadingScheduledTransactions = false
                )
            }
        }

        viewModelScope.launch {
            budgetTransactionsFetcher.data.collect { budgetTransactions ->
                _state.value = _state.value.copy(
                    transactions = budgetTransactions,
                    isLoadingTransactions = false
                )
            }
        }

    }



    override fun getTransactions(refresh: Boolean) {
        println("GETTING TRANSACTIONS $refresh")
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(
                    currentBudgetId = null,
                    isLoadingTransactions = true,
                    error = null,
                    currentPage = if (refresh) 0 else _state.value.currentPage
                )

                /*val response = service.getTransactions(
                    limit = _state.value.pageSize,
                    offset = _state.value.currentPage * _state.value.pageSize,
                    filters = _state.value.transactionFilters
                )*/

                if(refresh) {
                    transactionsFetcher.reset()
                }
                transactionsFetcher.loadNextPage()

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoadingTransactions = false
                )
            }
        }
    }

    override fun getBudgetTransactions(budgetId: String, refresh: Boolean) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoadingTransactions = true, error = null, currentBudgetId = budgetId)

                if(refresh){
                    budgetTransactionsFetcher.reset()
                }

                budgetTransactionsFetcher.loadNextPage()

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoadingTransactions = false
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
        getTransactions(refresh = false)
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
                _state.value = _state.value.copy(isLoadingBudgets = true, error = null)
                service.categorizeAllTransactions(
                    referenceDate = getCurrentDateTime(
                        TimeZone.currentSystemDefault()
                    ).date.formatDefault()
                )
                getBudgets()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoadingBudgets = false
                )
            }
        }
    }

    override fun categorizeUnbudgeted() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoadingBudgets = true, error = null)
                service.categorizeUnbudgeted(
                    referenceDate = getCurrentDateTime(
                        TimeZone.currentSystemDefault()
                    ).date.formatDefault()
                )
                getBudgets()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoadingBudgets = false
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
                _state.value = _state.value.copy(isLoadingTransactions = true, error = null)
                val newTransaction = service.addTransaction(transaction)
                if(state.value.currentBudgetId!=null) {
                    getBudgetTransactions(state.value.currentBudgetId!!)
                }
                else {
                    getTransactions(refresh = true)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoadingTransactions = false
                )
            }
        }
    }

    override fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoadingTransactions = true, error = null)
                val updatedTransaction = service.updateTransaction(transaction)
                if(state.value.currentBudgetId!=null) {
                    getBudgetTransactions(state.value.currentBudgetId!!)
                }
                else {
                    getTransactions(refresh = true)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoadingTransactions = false
                )
            }
        }
    }

    override fun deleteTransaction(id: String) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoadingTransactions = true, error = null)
                service.deleteTransaction(id)
                if(state.value.currentBudgetId!=null) {
                    getBudgetTransactions(state.value.currentBudgetId!!)
                }
                else {
                    getTransactions(refresh = true)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoadingTransactions = false
                )
            }
        }
    }

    override fun setSelectedTab(tabIndex: FinanceTab) {
        _state.value = _state.value.copy(selectedTab = tabIndex.ordinal)
    }

    override fun addAccount(account: Account) {
        viewModelScope.launch{
            try {
                _state.value = _state.value.copy(isLoadingAccounts = true, error = null)
                val newAccount = service.addAccount(account)
                _state.value = _state.value.copy(
                    accounts = _state.value.accounts + newAccount,
                    isLoadingAccounts = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoadingAccounts = false
                )
            }
        }
    }

    override fun updateAccount(account: Account) {
        viewModelScope.launch{
            try {
                _state.value = _state.value.copy(isLoadingAccounts = true, error = null)
                val updatedAccount = service.updateAccount(account)
                _state.value = _state.value.copy(
                    accounts = _state.value.accounts.map {
                        if (it.id == updatedAccount.id) updatedAccount else it
                    },
                    selectedAccount = if (_state.value.selectedAccount?.id == updatedAccount.id) updatedAccount else _state.value.selectedAccount,
                    isLoadingAccounts = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoadingAccounts = false
                )
            }
        }    }

    override fun deleteAccount(id: String) {
        viewModelScope.launch{
            try {
                _state.value = _state.value.copy(isLoadingAccounts = true, error = null)
                service.deleteAccount(id)
                _state.value = _state.value.copy(
                    accounts = _state.value.accounts.filter { it.id != id },
                    selectedAccount = if (_state.value.selectedAccount?.id == id) null else _state.value.selectedAccount,
                    isLoadingAccounts = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoadingAccounts = false
                )
            }
        }    }

    override fun getAccounts(){
        viewModelScope.launch{
            try {
                _state.value = _state.value.copy(isLoadingAccounts = true, error = null)
                val response = service.getAccounts()
                _state.value = _state.value.copy(
                    accounts = response,
                    isLoadingAccounts = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoadingAccounts = false
                )
            }
        }
    }

    override fun addBudget(budget: Budget) {
        viewModelScope.launch{
            try {
                _state.value = _state.value.copy(isLoadingBudgets = true, error = null)
                val newBudget = service.addBudget(budget)
                getBudgets()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoadingBudgets = false
                )
            }
        }    }

    override fun updateBudget(budget: Budget) {
        viewModelScope.launch{
            try {
                _state.value = _state.value.copy(isLoadingBudgets = true, error = null)
                val updatedBudget = service.updateBudget(budget)
               getBudgets()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoadingBudgets = false
                )
            }
        }    }

    override fun deleteBudget(id: String) {
        viewModelScope.launch{
            try {
                _state.value = _state.value.copy(isLoadingBudgets = true, error = null)
                service.deleteBudget(id)
                _state.value = _state.value.copy(
                    budgets = _state.value.budgets.filter { it.budget.id != id },
                    isLoadingBudgets = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoadingBudgets = false
                )
            }
        }    }


    override fun getBudgets() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoadingBudgets = true, error = null)
                budgetsFetcher.loadNextPage()  // Trigger the fetcher for the next page of budgets
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoadingBudgets = false
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
                _state.value = _state.value.copy(isLoadingSavingsGoals = true, error = null)
                val newGoal = service.addSavingsGoal(goal)
                val progress = service.getSavingsGoalProgress(newGoal.id ?: "")
                _state.value = _state.value.copy(
                    savingsGoals = _state.value.savingsGoals + newGoal,
                    savingsGoalProgress = _state.value.savingsGoalProgress + ((newGoal.id ?: "") to progress.percentageComplete),
                    isLoadingSavingsGoals = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoadingSavingsGoals = false
                )
            }
        }    }

    override fun updateSavingsGoal(goal: SavingsGoal) {
        viewModelScope.launch{
            try {
                _state.value = _state.value.copy(isLoadingSavingsGoals = true, error = null)
                val updatedGoal = service.updateSavingsGoal(goal)
                val progress = service.getSavingsGoalProgress(updatedGoal.id ?: "")
                _state.value = _state.value.copy(
                    savingsGoals = _state.value.savingsGoals.map {
                        if (it.id == updatedGoal.id) updatedGoal else it
                    },
                    savingsGoalProgress = _state.value.savingsGoalProgress + ((updatedGoal.id ?: "") to progress.percentageComplete),
                    isLoadingSavingsGoals = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoadingSavingsGoals = false
                )
            }
        }    }

    override fun deleteSavingsGoal(id: String) {
        viewModelScope.launch{
            try {
                _state.value = _state.value.copy(isLoadingSavingsGoals = true, error = null)
                service.deleteSavingsGoal(id)
                _state.value = _state.value.copy(
                    savingsGoals = _state.value.savingsGoals.filter { it.id != id },
                    savingsGoalProgress = _state.value.savingsGoalProgress - id,
                    isLoadingSavingsGoals = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoadingSavingsGoals = false
                )
            }
        }    }

    override fun getSavingsGoals() {
        viewModelScope.launch{
            try {
                _state.value = _state.value.copy(isLoadingSavingsGoals = true, error = null)
                val response = service.getSavingsGoals()
                _state.value = _state.value.copy(
                    savingsGoals = response,
                    isLoadingSavingsGoals = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoadingSavingsGoals = false
                )
            }
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
                _state.value = _state.value.copy(isLoadingTransactions = true, error = null, transactionFilters = _state.value.transactionFilters.copy(accountIds = listOf(accountId)))
                val transactionIds = service.importTransactions(text, accountId, skipDuplicates)
                val transactions = service.getTransactions()
                _state.value = _state.value.copy(
                    transactions = transactions.transactions,
                    isLoadingTransactions = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoadingTransactions = false
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

  

    fun updateTransactionFilters(filters: TransactionFilters) {
        _state.value = _state.value.copy(
            transactionFilters = filters,
            currentPage = 0
        )
        transactionsFetcher.reset()  // Reset the pagination for transactions
        getTransactions(refresh = true)
    }

    fun updateBudgetFilters(filters: BudgetFilters) {
        _state.value = _state.value.copy(
            budgetFilters = filters,
            currentPage = 0
        )
        budgetsFetcher.reset()  // Reset the pagination for budgets
        getBudgets()
    }

    fun updateScheduledTransactionFilters(filters: TransactionFilters) {
        _state.value = _state.value.copy(
            transactionFilters = filters,
            currentPage = 0
        )
        scheduledTransactionsFetcher.reset()  // Reset the pagination for scheduled transactions
        getScheduledTransactions(refresh = true)
    }

    override fun getScheduledTransactions(refresh: Boolean) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoadingScheduledTransactions = true, error = null)

                if (refresh) {
                    scheduledTransactionsFetcher.reset()
                }
                scheduledTransactionsFetcher.loadNextPage()

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoadingScheduledTransactions = false
                )
            }
        }
    }

    override fun addScheduledTransaction(transaction: ScheduledTransaction) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoadingScheduledTransactions = true, error = null)
                val newTransaction = service.addScheduledTransaction(transaction)
                getScheduledTransactions(refresh = true)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoadingScheduledTransactions = false
                )
            }
        }
    }

    override fun updateScheduledTransaction(transaction: ScheduledTransaction) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoadingScheduledTransactions = true, error = null)
                val updatedTransaction = service.updateScheduledTransaction(transaction)
                getScheduledTransactions(refresh = true)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoadingScheduledTransactions = false
                )
            }
        }
    }

    override fun deleteScheduledTransaction(id: String) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoadingScheduledTransactions = true, error = null)
                service.deleteScheduledTransaction(id)
                getScheduledTransactions(refresh = true)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message,
                    isLoadingScheduledTransactions = false
                )
            }
        }
    }

    fun resetError() {
        _state.value = _state.value.copy(error = null)
    }

    fun setCurrentBudgetId(budgetId: String) {
        _state.value = _state.value.copy(
            currentBudgetId = budgetId,
        )
    }


}