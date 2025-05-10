package com.esteban.ruano.workout_presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.esteban.ruano.core.routes.Routes
import com.esteban.ruano.core_ui.R
import com.esteban.ruano.core_ui.composables.Error
import com.esteban.ruano.core_ui.composables.Loading
import com.esteban.ruano.workout_presentation.intent.WorkoutIntent
import com.esteban.ruano.workout_presentation.ui.screens.WorkoutDayDetailScreen
import com.esteban.ruano.workout_presentation.ui.viewmodel.WorkoutDetailViewModel
import com.esteban.ruano.workout_presentation.ui.viewmodel.state.WorkoutDayDetailEffect
import kotlinx.coroutines.launch

@Composable
fun WorkoutDayDetailDestination(
    workoutId: String? = null,
    navController: NavController,
    viewModel: WorkoutDetailViewModel = hiltViewModel(),
    ) {

    val state = viewModel.viewState.collectAsState().value
    val coroutineScope = rememberCoroutineScope()


    /**
     * Handles navigation based on [WorkoutDayDetailEffect].
     *
     * @param effect The navigation event to handle.
     */
    fun handleNavigation(effect: WorkoutDayDetailEffect) {
        when (effect) {
            is WorkoutDayDetailEffect.NavigateUp -> navController.navigateUp()
            else -> {}
        }
    }

    LaunchedEffect(Unit) {
        // Collect and handle navigation effects.
        coroutineScope.launch { viewModel.effect.collect { handleNavigation(it) } }

        coroutineScope.launch {
           workoutId?.let {
               viewModel.performAction(
                   WorkoutIntent.FetchWorkoutDayById(it)
               )
           } ?: navController.navigateUp()
        }
    }

    when {
        state.errorMessage!=null -> {
            Error(
                message = stringResource(R.string.error_unknown),
            )
        }

        state.isLoading -> {
            Loading()
        }


        else -> {
            WorkoutDayDetailScreen(
                userIntent = {
                    viewModel.performAction(it)
                },
                state = state,
                workoutId = workoutId?: "",
                onClose = {
                    navController.navigateUp()
                },
                onStartWorkout = {
                    navController.navigate("${Routes.WORKOUT_PROGRESS}/$workoutId")
                },
                onAddExercisesClick = {
                    navController.navigate("${Routes.ADD_EXERCISES_TO_WORKOUT_DAY}/$workoutId")
                },
            )
        }
    }


}