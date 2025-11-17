package com.esteban.ruano.lifecommander.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.esteban.ruano.lifecommander.ui.components.ErrorScreen
import com.esteban.ruano.lifecommander.ui.components.LoadingScreen
import com.esteban.ruano.lifecommander.ui.viewmodels.*
import com.lifecommander.finance.ui.FinanceScreen
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun FinancialScreenDestination(
    modifier: Modifier = Modifier,
    coordinatorViewModel: FinanceCoordinatorViewModel = koinViewModel(),
    accountViewModel: AccountViewModel = koinViewModel(),
    transactionViewModel: TransactionViewModel = koinViewModel(),
    budgetViewModel: BudgetViewModel = koinViewModel(),
    scheduledTransactionViewModel: ScheduledTransactionViewModel = koinViewModel(),
    onOpenImporter: () -> Unit = {},
    onOpenBudgetTransactions: (String,String) -> Unit = {_,_ ->},
    onOpenCategoryKeywordMapper: () -> Unit = {},
    onOpenStatistics: () -> Unit = {},
) {
    val coordinatorState by coordinatorViewModel.state.collectAsState()
    val accountState by accountViewModel.state.collectAsState()
    val transactionState by transactionViewModel.state.collectAsState()
    val budgetState by budgetViewModel.state.collectAsState()
    val scheduledTransactionState by scheduledTransactionViewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        accountViewModel.getAccounts()
    }

    val combinedState = FinanceStateConverter.toFinanceState(
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

    when {
        combinedState.isLoading -> {
            LoadingScreen(
                message = "Loading financial data...",
                modifier = modifier
            )
        }
        combinedState.error != null -> {
            ErrorScreen(
                message = combinedState.error ?: "Failed to load financial data",
                onRetry = { accountViewModel.getAccounts() },
                modifier = modifier
            )
        }
        else -> {
            FinanceScreen(
                state = combinedState,
                actions = actions,
                onOpenImporter = onOpenImporter,
                onOpenBudgetTransactions = onOpenBudgetTransactions,
                onOpenCategoryKeywordMapper = onOpenCategoryKeywordMapper,
                onOpenStatistics = onOpenStatistics,
                isDesktop = true,
                modifier = modifier
            )
        }
    }
} 