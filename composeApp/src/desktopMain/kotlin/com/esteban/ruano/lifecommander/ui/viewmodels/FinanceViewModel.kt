package com.esteban.ruano.lifecommander.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esteban.ruano.lifecommander.models.finance.Category
import com.lifecommander.finance.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import com.esteban.ruano.lifecommander.services.finance.FinanceService


class FinanceViewModel(
    private val service: FinanceService
) : ViewModel(), FinanceActions {

    private val _state = MutableStateFlow(FinanceState())
    val state: StateFlow<FinanceState> = _state.asStateFlow()

    override suspend fun loadData() {
        try {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val accounts = service.getAccounts()
            val budgets = service.getBudgets()
            val savingsGoals = service.getSavingsGoals()
            val transactions = service.getTransactions()

            val budgetProgress = budgets.associate { budget ->
                (budget.id?:"") to service.getBudgetProgress(budget.id?:"")
            }
            val savingsGoalProgress = savingsGoals.associate { goal ->
                (goal.id?:"") to service.getSavingsGoalProgress(goal.id?:"")
            }

            _state.value = _state.value.copy(
                accounts = accounts,
                budgets = budgets,
                savingsGoals = savingsGoals,
                transactions = transactions,
                budgetProgress = budgetProgress,
                savingsGoalProgress = savingsGoalProgress,
                isLoading = false
            )
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                error = e.message,
                isLoading = false
            )
        }
    }

    override suspend fun selectAccount(account: Account?) {
        _state.value = _state.value.copy(selectedAccount = account)
        if (account != null) {
            loadTransactionsForAccount(account.id ?: "")
        }
    }

    private suspend fun loadTransactionsForAccount(accountId: String) {
        try {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val transactions = service.getTransactions(accountId = accountId)
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

    override suspend fun addTransaction(transaction: Transaction) {
        try {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val newTransaction = service.addTransaction(transaction)
            _state.value = _state.value.copy(
                transactions = _state.value.transactions + newTransaction,
                isLoading = false
            )
            if (transaction.type == TransactionType.EXPENSE) {
                updateBudgetProgress(transaction.category)
            }
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                error = e.message,
                isLoading = false
            )
        }
    }

    override suspend fun updateTransaction(transaction: Transaction) {
        try {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val updatedTransaction = service.updateTransaction(transaction)
            _state.value = _state.value.copy(
                transactions = _state.value.transactions.map {
                    if (it.id == updatedTransaction.id) updatedTransaction else it
                },
                isLoading = false
            )
            if (transaction.type == TransactionType.EXPENSE) {
                updateBudgetProgress(transaction.category)
            }
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                error = e.message,
                isLoading = false
            )
        }
    }

    override suspend fun deleteTransaction(id: String) {
        try {
            _state.value = _state.value.copy(isLoading = true, error = null)
            service.deleteTransaction(id)
            val deletedTransaction = _state.value.transactions.find { it.id == id }
            _state.value = _state.value.copy(
                transactions = _state.value.transactions.filter { it.id != id },
                isLoading = false
            )
            if (deletedTransaction?.type == TransactionType.EXPENSE) {
                updateBudgetProgress(deletedTransaction.category)
            }
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                error = e.message,
                isLoading = false
            )
        }
    }

    override suspend fun getTransactions(
        startDate: LocalDateTime?,
        endDate: LocalDateTime?,
        category: Category?,
        accountId: String?
    ): List<Transaction> {
        return service.getTransactions(startDate, endDate, category, accountId)
    }

    override suspend fun addAccount(account: Account) {
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

    override suspend fun updateAccount(account: Account) {
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
    }

    override suspend fun deleteAccount(id: String) {
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
    }

    override suspend fun getAccounts(): List<Account> {
        val response = service.getAccounts()
        _state.value = _state.value.copy(accounts = response)
        return response
    }

    override suspend fun addBudget(budget: Budget) {
        try {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val newBudget = service.addBudget(budget)
            val progress = service.getBudgetProgress(newBudget.id ?: "")
            _state.value = _state.value.copy(
                budgets = _state.value.budgets + newBudget,
                budgetProgress = _state.value.budgetProgress + ((newBudget.id?:"") to progress),
                isLoading = false
            )
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                error = e.message,
                isLoading = false
            )
        }
    }

    override suspend fun updateBudget(budget: Budget) {
        try {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val updatedBudget = service.updateBudget(budget)
            val progress = service.getBudgetProgress(updatedBudget.id?:"")
            _state.value = _state.value.copy(
                budgets = _state.value.budgets.map {
                    if (it.id == updatedBudget.id) updatedBudget else it
                },
                budgetProgress = _state.value.budgetProgress + ((updatedBudget.id?:"") to progress),
                isLoading = false
            )
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                error = e.message,
                isLoading = false
            )
        }
    }

    override suspend fun deleteBudget(id: String) {
        try {
            _state.value = _state.value.copy(isLoading = true, error = null)
            service.deleteBudget(id)
            _state.value = _state.value.copy(
                budgets = _state.value.budgets.filter { it.id != id },
                budgetProgress = _state.value.budgetProgress - id,
                isLoading = false
            )
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                error = e.message,
                isLoading = false
            )
        }
    }

    override suspend fun getBudgets(): List<Budget> {
        return service.getBudgets()
    }

    override suspend fun getBudgetProgress(budgetId: String): BudgetProgress? {
        return service.getBudgetProgress(budgetId)
    }

    override suspend fun addSavingsGoal(goal: SavingsGoal) {
        try {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val newGoal = service.addSavingsGoal(goal)
            val progress = service.getSavingsGoalProgress(newGoal.id?: "")
            _state.value = _state.value.copy(
                savingsGoals = _state.value.savingsGoals + newGoal,
                savingsGoalProgress = _state.value.savingsGoalProgress + ((newGoal.id?:"") to progress),
                isLoading = false
            )
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                error = e.message,
                isLoading = false
            )
        }
    }

    override suspend fun updateSavingsGoal(goal: SavingsGoal) {
        try {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val updatedGoal = service.updateSavingsGoal(goal)
            val progress = service.getSavingsGoalProgress(updatedGoal.id?:"")
            _state.value = _state.value.copy(
                savingsGoals = _state.value.savingsGoals.map {
                    if (it.id == updatedGoal.id) updatedGoal else it
                },
                savingsGoalProgress = _state.value.savingsGoalProgress + ((updatedGoal.id?:"") to progress),
                isLoading = false
            )
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                error = e.message,
                isLoading = false
            )
        }
    }

    override suspend fun deleteSavingsGoal(id: String) {
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
    }

    override suspend fun getSavingsGoals(): List<SavingsGoal> {
        return service.getSavingsGoals()
    }

    override suspend fun getSavingsGoalProgress(goalId: String): SavingsGoalProgress? {
        return service.getSavingsGoalProgress(goalId)
    }

    fun importTransactions(text: String, accountId: String, skipDuplicates: Boolean = true) {
        viewModelScope.launch{
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)
                val transactionIds = service.importTransactions(text, accountId, skipDuplicates)
                val transactions = service.getTransactions(accountId = accountId)
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

    private suspend fun updateBudgetProgress(category: Category) {
        try {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val budget = _state.value.budgets.find { it.category == category }
            if (budget != null) {
                val progress = service.getBudgetProgress(budget.id?:"")
                _state.value = _state.value.copy(
                    budgetProgress = _state.value.budgetProgress + ((budget.id?:"") to progress),
                    isLoading = false
                )
            }
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                error = e.message,
                isLoading = false
            )
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
}