package com.esteban.ruano.finance_presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.esteban.ruano.core_ui.composables.Error
import com.esteban.ruano.core_ui.composables.Loading
import com.esteban.ruano.finance_presentation.converter.FinanceStateConverter.toDesktopState
import com.esteban.ruano.finance_presentation.ui.intent.FinanceIntent
import com.esteban.ruano.finance_presentation.ui.viewmodel.FinanceViewModel
import kotlinx.coroutines.launch

@Composable
fun FinanceDestination(
    viewModel: FinanceViewModel = hiltViewModel(),
    navController: NavController,
) {
    val state =  viewModel.viewState.collectAsState().value
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            viewModel.handleIntent(FinanceIntent.GetAccounts)
        }
    }

    com.lifecommander.finance.ui.FinanceScreen(
        state = toDesktopState(state),
        onOpenImporter = {
            // Navigate to the transaction import screen
            navController.navigate("transaction_import")
        },
        actions = viewModel
    )
} 