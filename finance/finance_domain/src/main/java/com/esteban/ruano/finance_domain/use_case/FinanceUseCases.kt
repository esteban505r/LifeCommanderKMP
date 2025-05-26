package com.esteban.ruano.finance_domain.use_case

import com.esteban.ruano.finance_domain.repository.FinanceRepository
import com.lifecommander.finance.model.*
import com.esteban.ruano.lifecommander.models.finance.*

class FinanceUseCases(
    private val repository: FinanceRepository
) {
    // Transaction Use Cases
    suspend fun getTransactions(
        filter: String? = null,
        page: Int? = null,
        limit: Int? = null,
        filters: TransactionFilters
    ) = repository.getTransactions(filter, page, limit, filters)

    suspend fun getTransaction(transactionId: String) = repository.getTransaction(transactionId)
    
    suspend fun addTransaction(transaction: Transaction) = repository.addTransaction(transaction)
    
    suspend fun updateTransaction(transaction: Transaction) = repository.updateTransaction(transaction)
    
    suspend fun deleteTransaction(id: String) = repository.deleteTransaction(id)

    // Account Use Cases
    suspend fun getAccounts() = repository.getAccounts()
    
    suspend fun addAccount(account: Account) = repository.addAccount(account)
    
    suspend fun updateAccount(account: Account) = repository.updateAccount(account)
    
    suspend fun deleteAccount(id: String) = repository.deleteAccount(id)

    // Budget Use Cases
    suspend fun getBudgets(
        filters: BudgetFilters,
        referenceDate: String
    ) = repository.getBudgets(filters, referenceDate)
    
    suspend fun addBudget(budget: Budget) = repository.addBudget(budget)
    
    suspend fun updateBudget(budget: Budget) = repository.updateBudget(budget)
    
    suspend fun deleteBudget(id: String) = repository.deleteBudget(id)
    
    suspend fun getBudgetProgress(budgetId: String) = repository.getBudgetProgress(budgetId)
    
    suspend fun getBudgetTransactions(
        budgetId: String,
        referenceDate: String,
        filters: TransactionFilters
    ) = repository.getBudgetTransactions(budgetId, referenceDate, filters)

    // Savings Goal Use Cases
    suspend fun getSavingsGoals() = repository.getSavingsGoals()
    
    suspend fun addSavingsGoal(goal: SavingsGoal) = repository.addSavingsGoal(goal)
    
    suspend fun updateSavingsGoal(goal: SavingsGoal) = repository.updateSavingsGoal(goal)
    
    suspend fun deleteSavingsGoal(id: String) = repository.deleteSavingsGoal(id)
    
    suspend fun getSavingsGoalProgress(goalId: String) = repository.getSavingsGoalProgress(goalId)

    // Scheduled Transaction Use Cases
    suspend fun getScheduledTransactions(
        filter: String? = null,
        page: Int? = null,
        limit: Int? = null,
        filters: TransactionFilters
    ) = repository.getScheduledTransactions(filter, page, limit, filters)
    
    suspend fun addScheduledTransaction(transaction: ScheduledTransaction) = 
        repository.addScheduledTransaction(transaction)
    
    suspend fun updateScheduledTransaction(transaction: ScheduledTransaction) = 
        repository.updateScheduledTransaction(transaction)
    
    suspend fun deleteScheduledTransaction(id: String) = repository.deleteScheduledTransaction(id)

    // Utility Use Cases
    suspend fun categorizeAllTransactions(referenceDate: String) = 
        repository.categorizeAllTransactions(referenceDate)
    
    suspend fun categorizeUnbudgeted(referenceDate: String) = 
        repository.categorizeUnbudgeted(referenceDate)
    
    suspend fun importTransactions(text: String, accountId: String, skipDuplicates: Boolean = true) = 
        repository.importTransactions(text, accountId, skipDuplicates)
    
    suspend fun previewTransactionImport(text: String, accountId: String) = 
        repository.previewTransactionImport(text, accountId)
} 