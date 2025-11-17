package com.esteban.ruano.lifecommander.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.esteban.ruano.lifecommander.ui.components.ErrorScreen
import com.esteban.ruano.lifecommander.ui.components.LoadingScreen
import com.esteban.ruano.lifecommander.ui.viewmodels.*
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun FinanceStatisticsScreenDestination(
    modifier: Modifier = Modifier,
    statisticsViewModel: FinanceStatisticsViewModel = koinViewModel(),
    transactionViewModel: TransactionViewModel = koinViewModel(),
    accountViewModel: AccountViewModel = koinViewModel(),
    budgetViewModel: BudgetViewModel = koinViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val statisticsState by statisticsViewModel.state.collectAsState()
    val transactionState by transactionViewModel.state.collectAsState()
    val accountState by accountViewModel.state.collectAsState()
    val budgetState by budgetViewModel.state.collectAsState()

    // Load data only once when screen is first displayed
    LaunchedEffect(Unit) {
        statisticsViewModel.getStatistics()
        accountViewModel.getAccounts()
        budgetViewModel.getBudgets()
        // Only load transactions if statistics fail (as fallback)
    }

    val isLoading = statisticsState.isLoading || accountState.isLoading || budgetState.isLoading
    val error = statisticsState.error ?: accountState.error ?: budgetState.error

    when {
        isLoading && statisticsState.statistics == null -> {
            LoadingScreen(
                message = "Loading finance statistics...",
                modifier = modifier
            )
        }
        error != null && statisticsState.statistics == null -> {
            ErrorScreen(
                message = error,
                onRetry = { 
                    statisticsViewModel.getStatistics()
                    accountViewModel.getAccounts()
                    budgetViewModel.getBudgets()
                },
                modifier = modifier
            )
        }
        else -> {
            FinanceStatisticsScreen(
                statisticsViewModel = statisticsViewModel,
                transactionViewModel = transactionViewModel,
                accountViewModel = accountViewModel,
                budgetViewModel = budgetViewModel,
                onNavigateBack = onNavigateBack,
                modifier = modifier
            )
        }
    }
}

