package com.esteban.ruano.lifecommander.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.esteban.ruano.lifecommander.ui.components.ErrorScreen
import com.esteban.ruano.lifecommander.ui.components.LoadingScreen
import com.esteban.ruano.lifecommander.ui.screens.WorkoutScreen
import com.esteban.ruano.lifecommander.ui.viewmodels.WorkoutViewModel
import com.esteban.ruano.utils.DateUIUtils.formatDefault
import kotlinx.datetime.Clock
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun WorkoutScreenDestination() {
    val viewModel:WorkoutViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit){
        val now = Clock.System.now()
        val currentDay = now.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).date.dayOfWeek.value
        viewModel.getExercisesByDay(currentDay)
        viewModel.getWorkoutsCompletedPerDayThisWeek()
        
        // Load workout tracks for the current week
        val startOfWeek = now.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).date.minus(
            kotlinx.datetime.DatePeriod(days = currentDay - 1)
        )
        val endOfWeek = startOfWeek.plus(kotlinx.datetime.DatePeriod(days = 6))
        viewModel.getWorkoutTracksByDateRange(
            startDate = startOfWeek.formatDefault(),
            endDate = endOfWeek.formatDefault()
        )
    }

    when {
        state.isLoading -> {
            LoadingScreen(
                message = "Loading workout data...",
                modifier = androidx.compose.ui.Modifier
            )
        }
        state.isError -> {
            ErrorScreen(
                message = state.errorMessage ?: "Failed to load workout data",
                onRetry = {
                    val now = Clock.System.now()
                    val currentDay = now.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).date.dayOfWeek.value
                    viewModel.getExercisesByDay(currentDay)
                    viewModel.getWorkoutsCompletedPerDayThisWeek()
                },
                modifier = androidx.compose.ui.Modifier
            )
        }
        else -> {
            WorkoutScreen(
                state = state,
                onAdd = { exercise ->
                    viewModel.addExercise(exercise)
                },
                onUpdate = { exercise ->
                    viewModel.updateExercise(exercise)
                },
                onDelete = { id ->
                    viewModel.deleteExercise(id)
                },
                onDaySelected = { day ->
                    viewModel.getExercisesByDay(day)
                },
                onCompleteWorkout = { workoutDayId ->
                    viewModel.completeWorkout(workoutDayId)
                },
                onCompleteExercise = { exerciseId, workoutDayId ->
                    viewModel.completeExercise(exerciseId, workoutDayId)
                },
                onGetAllExercises = {
                    viewModel.getAllExercises()
                },
                onChangeAllExercisesMode = { value ->
                    viewModel.changeAllExercisesMode(value)
                },
                onBindExerciseToDay = { exerciseId, day, onResult ->
                    viewModel.bindExerciseToDay(exerciseId, day, onResult)
                },
                onUnbindExerciseFromDay = { exerciseId, day, onResult ->
                    viewModel.unbindExerciseFromDay(exerciseId, day, onResult)
                }
            )
        }
    }
}

