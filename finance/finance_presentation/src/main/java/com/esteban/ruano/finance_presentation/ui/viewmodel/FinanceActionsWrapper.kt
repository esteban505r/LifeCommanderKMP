package com.esteban.ruano.finance_presentation.ui.viewmodel

import com.esteban.ruano.lifecommander.models.finance.Budget
import com.esteban.ruano.lifecommander.models.finance.BudgetFilters
import com.esteban.ruano.lifecommander.models.finance.TransactionFilters
import com.esteban.ruano.lifecommander.ui.state.FinanceTab
import com.lifecommander.finance.model.FinanceActions
import com.lifecommander.finance.model.Account
import com.lifecommander.finance.model.SavingsGoal
import com.lifecommander.finance.model.ScheduledTransaction
import com.lifecommander.finance.model.Transaction
import kotlinx.datetime.LocalDate

class FinanceActionsWrapper(
    private val coordinatorViewModel: FinanceCoordinatorViewModel,
    private val accountViewModel: AccountViewModel,
    private val transactionViewModel: TransactionViewModel,
    private val budgetViewModel: BudgetViewModel,
    private val scheduledTransactionViewModel: ScheduledTransactionViewModel
) : FinanceActions {
    
    override fun setSelectedTab(tabIndex: FinanceTab) {
        coordinatorViewModel.setSelectedTab(tabIndex)
        // Load data for the selected tab
        when (tabIndex) {
            FinanceTab.ACCOUNTS -> accountViewModel.getAccounts()
            FinanceTab.TRANSACTIONS -> transactionViewModel.getTransactions(refresh = true)
            FinanceTab.SCHEDULED -> scheduledTransactionViewModel.getScheduledTransactions(refresh = true)
            FinanceTab.BUDGETS -> budgetViewModel.getBudgets()
        }
    }
    
    // Account actions
    override fun addAccount(account: Account) {
        accountViewModel.addAccount(account)
    }
    
    override fun updateAccount(account: Account) {
        accountViewModel.updateAccount(account)
    }
    
    override fun deleteAccount(id: String) {
        accountViewModel.deleteAccount(id)
    }
    
    override fun selectAccount(account: Account?) {
        accountViewModel.selectAccount(account)
        // When account is selected, update transaction filters and refresh transactions
        transactionViewModel.changeTransactionFilters(
            transactionViewModel.viewState.value.transactionFilters.copy(
                accountIds = account?.id?.let { listOf(it) }
            )
        ) {
            transactionViewModel.getTransactions(refresh = true)
        }
    }
    
    override fun getAccounts() {
        accountViewModel.getAccounts()
    }
    
    // Transaction actions
    override fun addTransaction(transaction: Transaction) {
        transactionViewModel.addTransaction(transaction)
    }
    
    override fun updateTransaction(transaction: Transaction) {
        transactionViewModel.updateTransaction(transaction)
    }
    
    override fun deleteTransaction(id: String) {
        transactionViewModel.deleteTransaction(id)
    }
    
    override fun getTransactions(refresh: Boolean) {
        transactionViewModel.getTransactions(refresh)
    }
    
    override fun changeTransactionFilters(filters: TransactionFilters, onSuccess: () -> Unit) {
        transactionViewModel.changeTransactionFilters(filters, onSuccess)
    }
    
    // Scheduled Transaction actions
    override fun addScheduledTransaction(transaction: ScheduledTransaction) {
        scheduledTransactionViewModel.addScheduledTransaction(transaction)
    }
    
    override fun updateScheduledTransaction(transaction: ScheduledTransaction) {
        scheduledTransactionViewModel.updateScheduledTransaction(transaction)
    }
    
    override fun deleteScheduledTransaction(id: String) {
        scheduledTransactionViewModel.deleteScheduledTransaction(id)
    }
    
    override fun getScheduledTransactions(refresh: Boolean) {
        scheduledTransactionViewModel.getScheduledTransactions(refresh)
    }
    
    // Budget actions
    override fun addBudget(budget: Budget) {
        budgetViewModel.addBudget(budget)
    }
    
    override fun updateBudget(budget: Budget) {
        budgetViewModel.updateBudget(budget)
    }
    
    override fun deleteBudget(id: String) {
        budgetViewModel.deleteBudget(id)
    }
    
    override fun getBudgets(reset: Boolean) {
        budgetViewModel.getBudgets(reset)
    }
    
    override fun getBudgetProgress(budgetId: String) {
        budgetViewModel.getBudgetProgress(budgetId)
    }
    
    override fun changeBudgetFilters(filters: BudgetFilters) {
        budgetViewModel.changeBudgetFilters(filters)
    }
    
    override fun changeBudgetBaseDate(date: LocalDate) {
        budgetViewModel.changeBudgetBaseDate(date)
    }
    
    override fun categorizeUnbudgeted() {
        budgetViewModel.categorizeUnbudgeted()
    }
    
    override fun categorizeAll() {
        budgetViewModel.categorizeAll()
    }
    
    override fun getBudgetTransactions(budgetId: String, refresh: Boolean) {
        budgetViewModel.getBudgetTransactions(budgetId, refresh)
    }
    
    // Savings goal actions - TODO: Implement when SavingsGoalViewModel is created
    override fun addSavingsGoal(goal: SavingsGoal) {
        // TODO: Implement
    }
    
    override fun updateSavingsGoal(goal: SavingsGoal) {
        // TODO: Implement
    }
    
    override fun deleteSavingsGoal(id: String) {
        // TODO: Implement
    }
    
    override fun getSavingsGoals() {
        // TODO: Implement
    }
    
    override fun getSavingsGoalProgress(goalId: String) {
        // TODO: Implement
    }
}

