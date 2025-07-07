package com.esteban.ruano.finance_presentation.ui.intent

import com.esteban.ruano.core_ui.utils.SnackbarType
import com.esteban.ruano.core_ui.view_model.Effect
import com.esteban.ruano.core_ui.view_model.UserIntent
import com.esteban.ruano.lifecommander.ui.state.FinanceTab
import com.esteban.ruano.lifecommander.models.finance.Budget
import com.esteban.ruano.lifecommander.models.finance.BudgetFilters
import com.esteban.ruano.lifecommander.models.finance.TransactionFilters
import com.lifecommander.finance.model.Transaction
import com.lifecommander.finance.model.Account
import com.lifecommander.finance.model.SavingsGoal
import com.lifecommander.finance.model.ScheduledTransaction
import java.time.LocalDate

sealed class FinanceIntent : UserIntent{
    // Tab Selection
    data class ChangeTab(val tab: FinanceTab) : FinanceIntent()
    
    // Transaction Intents
    data class GetTransactions(val refresh: Boolean = false) : FinanceIntent()
    data class AddTransaction(val transaction: Transaction) : FinanceIntent()
    data class UpdateTransaction(val transaction: Transaction) : FinanceIntent()
    data class DeleteTransaction(val id: String) : FinanceIntent()
    data class ChangeTransactionFilters(val filters: TransactionFilters) : FinanceIntent()
    
    // Account Intents
    object GetAccounts : FinanceIntent()
    data class AddAccount(val account: Account) : FinanceIntent()
    data class UpdateAccount(val account: Account) : FinanceIntent()
    data class DeleteAccount(val id: String) : FinanceIntent()
    data class SelectAccount(val account: Account?) : FinanceIntent()
    
    // Budget Intents
    data class GetBudgets(val refresh: Boolean = false) : FinanceIntent()
    data class AddBudget(val budget: Budget) : FinanceIntent()
    data class UpdateBudget(val budget: Budget) : FinanceIntent()
    data class DeleteBudget(val id: String) : FinanceIntent()
    data class ChangeBudgetFilters(val filters: BudgetFilters) : FinanceIntent()
    data class ChangeBudgetBaseDate(val date: LocalDate) : FinanceIntent()
    
    // Savings Goal Intents
    object GetSavingsGoals : FinanceIntent()
    data class AddSavingsGoal(val goal: SavingsGoal) : FinanceIntent()
    data class UpdateSavingsGoal(val goal: SavingsGoal) : FinanceIntent()
    data class DeleteSavingsGoal(val id: String) : FinanceIntent()
    
    // Scheduled Transaction Intents
    data class GetScheduledTransactions(val refresh: Boolean = false) : FinanceIntent()
    data class AddScheduledTransaction(val transaction: ScheduledTransaction) : FinanceIntent()
    data class UpdateScheduledTransaction(val transaction: ScheduledTransaction) : FinanceIntent()
    data class DeleteScheduledTransaction(val id: String) : FinanceIntent()
    
    // Utility Intents
    data class CategorizeAllTransactions(val referenceDate: String) : FinanceIntent()
    data class CategorizeUnbudgeted(val referenceDate: String) : FinanceIntent()
    data class ImportTransactions(
        val text: String,
        val accountId: String,
        val skipDuplicates: Boolean
    ) : FinanceIntent()
    data class PreviewTransactionImport(
        val text: String,
        val accountId: String
    ) : FinanceIntent()
    data class ShowSnackBar(
        val message: String,
        val type: com.esteban.ruano.core_ui.utils.SnackbarType
    ) : FinanceIntent()
}

sealed class FinanceEffect : Effect{
    data object NavigateUp: FinanceEffect()
    data class ShowSnackBar(val message:String, val type:SnackbarType): FinanceEffect()
}

