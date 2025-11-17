package com.esteban.ruano.lifecommander.ui.viewmodels

import com.esteban.ruano.lifecommander.ui.state.FinanceState
import com.esteban.ruano.lifecommander.ui.state.FinanceTab

object FinanceStateConverter {
    fun toFinanceState(
        coordinatorState: FinanceCoordinatorState,
        accountState: AccountState,
        transactionState: TransactionState,
        budgetState: BudgetState,
        scheduledTransactionState: ScheduledTransactionState
    ): FinanceState {
        return FinanceState(
            selectedTab = when (coordinatorState.selectedTab) {
                FinanceTab.ACCOUNTS -> 0
                FinanceTab.TRANSACTIONS -> 1
                FinanceTab.BUDGETS -> 3
                FinanceTab.SCHEDULED -> 2
            },
            accounts = accountState.accounts,
            transactions = transactionState.transactions,
            scheduledTransactions = scheduledTransactionState.scheduledTransactions,
            budgets = budgetState.budgets,
            savingsGoals = emptyList(), // TODO: Add SavingsGoalViewModel if needed
            savingsGoalProgress = emptyMap(), // TODO: Add SavingsGoalViewModel if needed
            selectedAccount = accountState.selectedAccount,
            transactionFilters = transactionState.transactionFilters,
            budgetFilters = budgetState.budgetFilters,
            budgetBaseDate = budgetState.budgetBaseDate,
            importPreview = transactionState.importPreview,
            isLoading = accountState.isLoading || transactionState.isLoading || 
                        budgetState.isLoading || scheduledTransactionState.isLoading,
            isLoadingAccounts = accountState.isLoading,
            isLoadingTransactions = transactionState.isLoadingTransactions,
            isLoadingScheduledTransactions = scheduledTransactionState.isLoading,
            isLoadingBudgets = budgetState.isLoading,
            isLoadingSavingsGoals = false,
            error = accountState.error ?: transactionState.error ?: 
                   budgetState.error ?: scheduledTransactionState.error,
            currentPage = transactionState.currentPage,
            pageSize = transactionState.pageSize,
            totalTransactions = transactionState.totalTransactions,
            totalScheduledTransactions = scheduledTransactionState.totalScheduledTransactions,
            currentBudgetId = transactionState.currentBudgetId
        )
    }
}

