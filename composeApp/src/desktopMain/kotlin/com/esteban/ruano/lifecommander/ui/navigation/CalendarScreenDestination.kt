package com.esteban.ruano.lifecommander.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.esteban.ruano.lifecommander.ui.screens.CalendarScreen
import com.esteban.ruano.lifecommander.ui.viewmodels.CalendarViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CalendarScreenDestination(
    modifier: Modifier = Modifier,
    calendarViewModel: CalendarViewModel = koinViewModel(),
    onTaskClick: (String) -> Unit = {},
    onHabitClick: (String) -> Unit = {}
) {
    CalendarScreen(
        modifier = modifier,
        calendarViewModel = calendarViewModel,
        onTaskClick = onTaskClick,
        onHabitClick = onHabitClick
    )
} 