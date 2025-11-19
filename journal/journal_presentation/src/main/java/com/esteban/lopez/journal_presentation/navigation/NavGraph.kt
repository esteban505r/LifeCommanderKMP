package com.esteban.lopez.journal_presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.esteban.ruano.core.routes.Routes
import com.esteban.ruano.journal_presentation.intent.JournalIntent
import com.esteban.ruano.journal_presentation.ui.JournalHistoryScreen
import com.esteban.ruano.journal_presentation.navigation.JournalDestination
import com.esteban.ruano.journal_presentation.ui.viewmodel.JournalViewModel
import com.esteban.ruano.utils.DateUIUtils.getCurrentDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.DateTimeUnit

fun NavGraphBuilder.journalGraph(
    navController: NavController
) {
    composable(Routes.BASE.JOURNAL.name) {
        JournalDestination(navController = navController)
    }
    
    composable(Routes.JOURNAL_HISTORY) {
        JournalHistoryDestination(
            navController = navController
        )
    }
}

@Composable
fun JournalHistoryDestination(
    viewModel: JournalViewModel = hiltViewModel(),
    navController: NavController
) {
    val state = viewModel.viewState.collectAsState().value
    
    LaunchedEffect(Unit) {
        // Load initial history (last 30 days)
        val today = getCurrentDateTime(TimeZone.currentSystemDefault()).date
        val startDate = today.minus(30, DateTimeUnit.DAY)
        viewModel.performAction(JournalIntent.GetHistoryByDateRange(startDate, today))
    }
    
    JournalHistoryScreen(
        journalEntries = state.journalHistory,
        onDateRangeSelected = { startDate, endDate ->
            viewModel.performAction(JournalIntent.GetHistoryByDateRange(startDate, endDate))
        },
        onBack = {
            navController.navigateUp()
        }
    )
}

