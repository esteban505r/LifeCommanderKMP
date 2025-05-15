package com.esteban.ruano.lifecommander.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.esteban.ruano.lifecommander.ui.viewmodels.FinanceViewModel
import com.lifecommander.finance.ui.FinanceScreen
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun FinancialScreenDestination(
    modifier: Modifier = Modifier,
    financialViewModel: FinanceViewModel = koinViewModel(),
) {
    val state by financialViewModel.state.collectAsState()

    FinanceScreen(
        modifier = modifier,
        state = state,
        actions = financialViewModel,
    )
} 