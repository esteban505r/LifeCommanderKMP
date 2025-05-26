package com.lifecommander.finance.model

import com.esteban.ruano.lifecommander.models.finance.Budget
import com.esteban.ruano.lifecommander.models.finance.BudgetFilters
import com.esteban.ruano.lifecommander.models.finance.Category
import com.esteban.ruano.lifecommander.models.finance.TransactionFilters
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

interface FinanceActions {
    // Account actions
    fun addAccount(account: Account)
    fun updateAccount(account: Account)
    fun deleteAccount(id: String)
    fun selectAccount(account: Account?)
    fun getAccounts()
    
    // Transaction actions
    fun addTransaction(transaction: Transaction)
    fun updateTransaction(transaction: Transaction)
    fun deleteTransaction(id: String)
    fun getTransactions(
        refresh: Boolean = false,
    )

    fun changeTransactionFilters(
        filters: TransactionFilters,
        onSuccess: () -> Unit,
    )
    
    // Scheduled Transaction actions
    fun addScheduledTransaction(transaction: ScheduledTransaction)
    fun updateScheduledTransaction(transaction: ScheduledTransaction)
    fun deleteScheduledTransaction(id: String)
    fun getScheduledTransactions(
        refresh: Boolean = false,
    )
    
    // Budget actions
    fun addBudget(budget: Budget)
    fun updateBudget(budget: Budget)
    fun deleteBudget(id: String)
    fun getBudgets()
    fun getBudgetProgress(budgetId: String)
    
    // Savings goal actions
    fun addSavingsGoal(goal: SavingsGoal)
    fun updateSavingsGoal(goal: SavingsGoal)
    fun deleteSavingsGoal(id: String)
    fun getSavingsGoals()
    fun getSavingsGoalProgress(goalId: String)
    
    // Data loading
    fun changeBudgetFilters(filters: BudgetFilters)
    fun changeBudgetBaseDate(date: LocalDate)
    fun categorizeUnbudgeted()
    fun categorizeAll()
    fun getBudgetTransactions(budgetId: String)
}