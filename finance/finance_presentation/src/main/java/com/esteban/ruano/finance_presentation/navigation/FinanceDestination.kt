package com.esteban.ruano.finance_presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.esteban.ruano.finance_presentation.converter.FinanceStateConverter.toDesktopState
import com.esteban.ruano.finance_presentation.ui.viewmodel.*

@Composable
fun FinanceDestination(
    coordinatorViewModel: FinanceCoordinatorViewModel = hiltViewModel(),
    accountViewModel: AccountViewModel = hiltViewModel(),
    transactionViewModel: TransactionViewModel = hiltViewModel(),
    budgetViewModel: BudgetViewModel = hiltViewModel(),
    scheduledTransactionViewModel: ScheduledTransactionViewModel = hiltViewModel(),
    navController: NavController,
) {
    val coordinatorState by coordinatorViewModel.viewState.collectAsState()
    val accountState by accountViewModel.viewState.collectAsState()
    val transactionState by transactionViewModel.viewState.collectAsState()
    val budgetState by budgetViewModel.viewState.collectAsState()
    val scheduledTransactionState by scheduledTransactionViewModel.viewState.collectAsState()

    LaunchedEffect(Unit) {
        accountViewModel.getAccounts()
    }

    val combinedState = toDesktopState(
        coordinatorState = coordinatorState,
        accountState = accountState,
        transactionState = transactionState,
        budgetState = budgetState,
        scheduledTransactionState = scheduledTransactionState
    )

    val actions = FinanceActionsWrapper(
        coordinatorViewModel = coordinatorViewModel,
        accountViewModel = accountViewModel,
        transactionViewModel = transactionViewModel,
        budgetViewModel = budgetViewModel,
        scheduledTransactionViewModel = scheduledTransactionViewModel
    )

    com.lifecommander.finance.ui.FinanceScreen(
        state = combinedState,
        onOpenImporter = {
            // Navigate to the transaction import screen
            navController.navigate("transaction_import")
        },
        actions = actions
    )
} 