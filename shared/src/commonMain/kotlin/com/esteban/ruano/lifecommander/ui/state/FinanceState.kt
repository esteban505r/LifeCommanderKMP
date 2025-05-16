package com.lifecommander.finance.model

import kotlinx.serialization.Serializable

@Serializable
data class FinanceState(
    val accounts: List<Account> = emptyList(),
    val selectedAccount: Account? = null,
    val budgets: List<Budget> = emptyList(),
    val savingsGoals: List<SavingsGoal> = emptyList(),
    val transactions: List<Transaction> = emptyList(),
    val importPreview: TransactionImportPreview? = null,
    val budgetProgress: Map<String, BudgetProgress> = emptyMap(),
    val savingsGoalProgress: Map<String, SavingsGoalProgress> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null
) 