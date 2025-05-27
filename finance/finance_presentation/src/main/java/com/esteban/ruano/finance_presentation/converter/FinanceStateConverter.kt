package com.esteban.ruano.finance_presentation.converter

import com.esteban.ruano.finance_presentation.ui.viewmodel.state.FinanceState
import com.esteban.ruano.lifecommander.models.finance.BudgetProgress
import com.esteban.ruano.lifecommander.ui.state.FinanceState as DesktopFinanceState

object FinanceStateConverter {
    fun toDesktopState(mobileState: FinanceState): DesktopFinanceState {
        return DesktopFinanceState(
            accounts = mobileState.accounts,
            transactions = mobileState.transactions,
            scheduledTransactions = mobileState.scheduledTransactions,
            budgets = mobileState.budgets,
            savingsGoals = mobileState.savingsGoals,
            savingsGoalProgress = mobileState.savingsGoalProgress,
            selectedAccount = mobileState.selectedAccount,
            transactionFilters = mobileState.transactionFilters,
            budgetFilters = mobileState.budgetFilters,
            budgetBaseDate = mobileState.budgetBaseDate,
            importPreview = mobileState.importPreview,
            isLoading = mobileState.isLoading,
            error = mobileState.error,
            currentPage = mobileState.currentPage,
            pageSize = mobileState.pageSize,
            totalTransactions = mobileState.totalTransactions,
            totalScheduledTransactions = mobileState.totalScheduledTransactions
        )
    }

    fun toMobileState(desktopState: DesktopFinanceState): FinanceState {
        return FinanceState(
            accounts = desktopState.accounts,
            transactions = desktopState.transactions,
            scheduledTransactions = desktopState.scheduledTransactions,
            budgets = desktopState.budgets,
            savingsGoals = desktopState.savingsGoals,
            savingsGoalProgress = desktopState.savingsGoalProgress,
            selectedAccount = desktopState.selectedAccount,
            transactionFilters = desktopState.transactionFilters,
            budgetFilters = desktopState.budgetFilters,
            budgetBaseDate = desktopState.budgetBaseDate,
            importPreview = desktopState.importPreview,
            isLoading = desktopState.isLoading,
            error = desktopState.error,
            currentPage = desktopState.currentPage,
            pageSize = desktopState.pageSize,
            totalTransactions = desktopState.totalTransactions,
            totalScheduledTransactions = desktopState.totalScheduledTransactions
        )
    }
} 