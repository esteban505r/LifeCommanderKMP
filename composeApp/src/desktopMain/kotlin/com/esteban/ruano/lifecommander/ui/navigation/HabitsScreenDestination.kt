package com.esteban.ruano.lifecommander.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.esteban.ruano.lifecommander.ui.components.ErrorScreen
import com.esteban.ruano.lifecommander.ui.components.LoadingScreen
import com.esteban.ruano.lifecommander.ui.screens.HabitsScreen
import com.lifecommander.models.Habit
import com.lifecommander.models.Reminder
import org.koin.compose.viewmodel.koinViewModel
import ui.viewmodels.HabitsViewModel

@Composable
fun HabitsScreenDestination(
    modifier: Modifier = Modifier,
    habitsViewModel: HabitsViewModel = koinViewModel(),
    onHabitClick: (Habit) -> Unit
) {
    val habits by habitsViewModel.habits.collectAsState()
    val habitsLoading by habitsViewModel.loading.collectAsState()
    val habitsError by habitsViewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        habitsViewModel.getHabits()
    }

    when {
        habitsLoading -> {
            LoadingScreen(
                message = "Loading habits...",
                modifier = modifier
            )
        }
        habitsError != null -> {
            ErrorScreen(
                message = habitsError ?: "Failed to load habits",
                onRetry = { habitsViewModel.getHabits() },
                modifier = modifier
            )
        }
        else -> {
            HabitsScreen(
                habits = habits,
                habitsLoading = habitsLoading,
                onHabitClick = onHabitClick,
                onReload = {
                    habitsViewModel.getHabits()
                },
                onAddHabit = { name, note, frequency, dateTime, reminders ->
                    habitsViewModel.addHabit(name, note, frequency, dateTime, reminders)
                },
                onUpdateHabit = { id, habit ->
                    habitsViewModel.updateHabit(id, habit)
                },
                onDelete = { habit ->
                    habitsViewModel.deleteHabit(habit.id)
                },
                onCheckedHabit = { habit, checked, onComplete ->
                    habitsViewModel.changeCheckHabit(habit.id, checked)
                }
            )
        }
    }
} 