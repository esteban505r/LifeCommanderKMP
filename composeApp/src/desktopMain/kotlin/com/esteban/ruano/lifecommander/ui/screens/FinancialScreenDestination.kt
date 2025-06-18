package com.esteban.ruano.lifecommander.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.esteban.ruano.lifecommander.ui.components.ErrorScreen
import com.esteban.ruano.lifecommander.ui.components.LoadingScreen
import com.esteban.ruano.lifecommander.ui.viewmodels.FinanceViewModel
import com.lifecommander.finance.ui.FinanceScreen
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun FinancialScreenDestination(
    modifier: Modifier = Modifier,
    financialViewModel: FinanceViewModel = koinViewModel(),
    onOpenImporter: () -> Unit = {},
    onOpenBudgetTransactions: (String) -> Unit = {},
    onOpenCategoryKeywordMapper: () -> Unit = {},
) {
    val state by financialViewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        financialViewModel.getAccounts()
    }

    when {
        state.isLoading -> {
            LoadingScreen(
                message = "Loading financial data...",
                modifier = modifier
            )
        }
        state.error != null -> {
            ErrorScreen(
                message = state.error ?: "Failed to load financial data",
                onRetry = { financialViewModel.getAccounts() },
                modifier = modifier
            )
        }
        else -> {
    FinanceScreen(
        state = state,
        actions = financialViewModel,
        onOpenImporter = onOpenImporter,
        onOpenBudgetTransactions = onOpenBudgetTransactions,
        onOpenCategoryKeywordMapper = onOpenCategoryKeywordMapper,
                isDesktop = true,
                modifier = modifier
    )
        }
    }
} 