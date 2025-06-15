package com.esteban.ruano.lifecommander.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.esteban.ruano.lifecommander.ui.screens.WorkoutScreen
import com.esteban.ruano.lifecommander.ui.viewmodels.WorkoutViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun WorkoutScreenDestination() {
    val viewModel:WorkoutViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit){
        val now = Clock.System.now()
        val currentDay = now.toLocalDateTime(
            kotlinx.datetime.TimeZone.currentSystemDefault()
        ).dayOfWeek.value
        viewModel.getExercisesByDay(currentDay)
    }

    WorkoutScreen(
        state = state,
        onAdd = viewModel::addExercise,
        onUpdate = viewModel::updateExercise,
        onDelete = viewModel::deleteExercise,
        onDaySelected = viewModel::getExercisesByDay
    )
}

