package com.esteban.ruano.lifecommander.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.esteban.ruano.lifecommander.ui.components.ErrorScreen
import com.esteban.ruano.lifecommander.ui.components.LoadingScreen
import com.esteban.ruano.lifecommander.ui.viewmodels.FinanceViewModel
import com.esteban.ruano.lifecommander.ui.viewmodels.TimersViewModel
import org.koin.compose.viewmodel.koinViewModel
import kotlinx.datetime.*
import kotlinx.datetime.toJavaLocalDate
import ui.viewmodels.DashboardViewModel

@Composable
fun StatisticsScreenDestination(
    modifier: Modifier = Modifier,
    dashboardViewModel: DashboardViewModel = koinViewModel(),
    financeViewModel: FinanceViewModel = koinViewModel(),
    timersViewModel: TimersViewModel = koinViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val financeState by financeViewModel.state.collectAsState()
    val dashboardLoading by dashboardViewModel.loading.collectAsState()
    val financeLoading = financeState.isLoadingBudgets

    LaunchedEffect(Unit) {
        // Load all necessary data for statistics
        dashboardViewModel.refreshDashboard()
        financeViewModel.getBudgets()
        
        // Load pomodoros for the current week
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val startOfWeek = now.minus(DatePeriod(days = now.dayOfWeek.value - 1))
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
        financeState.error != null -> {
            ErrorScreen(
                message = financeState.error ?: "Failed to load statistics",
                onRetry = { 
                    dashboardViewModel.refreshDashboard()
                    financeViewModel.getBudgets()
                },
                modifier = modifier
            )
        }
        else -> {
            StatisticsScreen(
                dashboardViewModel = dashboardViewModel,
                financeViewModel = financeViewModel,
                timersViewModel = timersViewModel,
                onNavigateBack = onNavigateBack,
                modifier = modifier
            )
        }
    }
} 