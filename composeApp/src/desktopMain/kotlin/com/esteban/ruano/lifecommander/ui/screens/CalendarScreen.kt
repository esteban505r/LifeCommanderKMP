package com.esteban.ruano.lifecommander.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.esteban.ruano.lifecommander.ui.components.ErrorScreen
import com.esteban.ruano.lifecommander.ui.components.LoadingScreen
import com.esteban.ruano.lifecommander.ui.viewmodels.CalendarViewModel
import kotlinx.datetime.toKotlinLocalDate
import org.koin.compose.viewmodel.koinViewModel
import ui.components.CalendarComposable

@Composable
fun CalendarScreen(
    modifier: Modifier = Modifier,
    calendarViewModel: CalendarViewModel = koinViewModel(),
    onTaskClick: (String) -> Unit,
    onHabitClick: (String) -> Unit
) {
    val tasks by calendarViewModel.tasks.collectAsState()
    val habits by calendarViewModel.habits.collectAsState()
    val transactions by calendarViewModel.transactions.collectAsState()
    val isLoading by calendarViewModel.isLoading.collectAsState()
    val error by calendarViewModel.error.collectAsState()

    CalendarComposable(
        onTaskClick = onTaskClick,
        onHabitClick = onHabitClick,
        tasks = tasks,
        habits = habits,
        transactions = transactions,
        onRefresh = { startDate, endDate -> calendarViewModel.refresh(
            startDate = startDate.toKotlinLocalDate(),
            endDate = endDate.toKotlinLocalDate()
        ) },
        isLoading = isLoading,
        error = error,
    )
} 