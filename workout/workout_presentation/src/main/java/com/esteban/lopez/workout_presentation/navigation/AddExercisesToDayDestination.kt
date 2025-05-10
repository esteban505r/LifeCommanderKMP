package com.esteban.ruano.workout_presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.esteban.ruano.core.utils.UiText
import com.esteban.ruano.core_ui.R
import com.esteban.ruano.core_ui.composables.Error
import com.esteban.ruano.core_ui.composables.Loading
import com.esteban.ruano.core_ui.utils.CustomSnackBarVisuals
import com.esteban.ruano.core_ui.utils.CustomSnackbarVisualsWithUiText
import com.esteban.ruano.core_ui.utils.SnackbarController
import com.esteban.ruano.core_ui.utils.SnackbarEvent
import com.esteban.ruano.core_ui.utils.SnackbarType
import com.esteban.ruano.workout_presentation.intent.ExercisesEffect
import com.esteban.ruano.workout_presentation.intent.ExercisesIntent
import com.esteban.ruano.workout_presentation.intent.WorkoutIntent
import com.esteban.ruano.workout_presentation.ui.screens.AddExerciseToDayScreen
import com.esteban.ruano.workout_presentation.ui.screens.ExerciseScreen
import com.esteban.ruano.workout_presentation.ui.viewmodel.ExercisesViewModel
import com.esteban.ruano.workout_presentation.ui.viewmodel.WorkoutDetailViewModel
import com.esteban.ruano.workout_presentation.ui.viewmodel.state.WorkoutDayDetailEffect
import kotlinx.coroutines.launch

@Composable
fun AddExercisesToDayDestination(
    workoutDayId: String? = null,
    viewModel: ExercisesViewModel = hiltViewModel(),
    workoutDayViewmodel: WorkoutDetailViewModel = hiltViewModel(),
    navController: NavController,
) {

    val state = viewModel.viewState.collectAsState().value
    val coroutineScope = rememberCoroutineScope()
    val workoutDetailState = workoutDayViewmodel.viewState.collectAsState().value
    val context = LocalContext.current

    /**
     * Handles navigation based on [ExercisesEffect].
     *
     * @param effect The navigation event to handle.
     */
    fun handleNavigation(effect: ExercisesEffect) {
        when (effect) {
            is ExercisesEffect.NavigateUp -> navController.navigateUp()
            is ExercisesEffect.ShowSnackbarErrorMessage -> {
                coroutineScope.launch {
                    SnackbarController.sendEvent(
                        SnackbarEvent(
                            CustomSnackbarVisualsWithUiText.fromType(
                                SnackbarType.ERROR,
                                message = UiText.StringResource(R.string.error_unknown)
                            )
                        )
                    )
                }
            }
        }
    }

    /** Handles navigation based on [WorkoutDayDetailEffect].
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

        coroutineScope.launch { workoutDayViewmodel.effect.collect { handleNavigation(it) } }


        coroutineScope.launch {
            viewModel.performAction(ExercisesIntent.FetchExercises)
            workoutDayId?.let {  workoutDayViewmodel.performAction(WorkoutIntent.FetchWorkoutDayById(it)) }?: navController.navigateUp()
        }
    }

    when {
        state.errorMessage!=null -> {
            Error(
                message = stringResource(R.string.error_unknown),
            )
        }

        state.loading -> {
            Loading()
        }


        else -> {
            AddExerciseToDayScreen(
                workoutDayId = workoutDayId?: "",
                userIntent = {
                    viewModel.performAction(it)
                },
                workoutDayState = workoutDetailState,
                detailUserIntent = {
                    workoutDayViewmodel.performAction(it)
                },
                onClose =  {
                    navController.navigateUp()
                },
                state = state,
            )
        }
        }
    }