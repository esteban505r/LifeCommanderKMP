package com.esteban.ruano.lifecommander.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.esteban.ruano.lifecommander.ui.components.ErrorScreen
import com.esteban.ruano.lifecommander.ui.components.LoadingScreen
import com.esteban.ruano.lifecommander.ui.viewmodels.*
import com.esteban.ruano.lifecommander.ui.viewmodels.TimersViewModel
import com.esteban.ruano.utils.DateUIUtils.getCurrentDateTime
import org.koin.compose.viewmodel.koinViewModel
import kotlinx.datetime.*
import kotlinx.datetime.toJavaLocalDate
import ui.viewmodels.DashboardViewModel

@Composable
fun StatisticsScreenDestination(
    modifier: Modifier = Modifier,
    dashboardViewModel: DashboardViewModel = koinViewModel(),
    budgetViewModel: BudgetViewModel = koinViewModel(),
    timersViewModel: TimersViewModel = koinViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val budgetState by budgetViewModel.state.collectAsState()
    val dashboardLoading by dashboardViewModel.loading.collectAsState()
    val financeLoading = budgetState.isLoading

    LaunchedEffect(Unit) {
        // Load all necessary data for statistics
        dashboardViewModel.refreshDashboard()
        budgetViewModel.getBudgets()
        
        // Load pomodoros for the current week
        val now = getCurrentDateTime(TimeZone.currentSystemDefault()).date
        val startOfWeek = now.minus(DatePeriod(days = now.dayOfWeek.ordinal))
        timersViewModel.loadPomodorosByDateRange(
            startOfWeek.toJavaLocalDate(),
            startOfWeek.plus(DatePeriod(days = 6)).toJavaLocalDate()
        )
    }

    when {
        dashboardLoading || financeLoading -> {
            LoadingScreen(
                message = "Loading statistics...",
                modifier = modifier
            )
        }
        budgetState.error != null -> {
            ErrorScreen(
                message = budgetState.error ?: "Failed to load statistics",
                onRetry = { 
                    dashboardViewModel.refreshDashboard()
                    budgetViewModel.getBudgets()
                },
                modifier = modifier
            )
        }
        else -> {
            StatisticsScreen(
                dashboardViewModel = dashboardViewModel,
                budgetViewModel = budgetViewModel,
                timersViewModel = timersViewModel,
                onNavigateBack = onNavigateBack,
                modifier = modifier
            )
        }
    }
}