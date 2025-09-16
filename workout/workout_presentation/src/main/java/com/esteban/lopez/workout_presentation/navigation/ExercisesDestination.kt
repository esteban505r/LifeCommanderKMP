package com.esteban.ruano.workout_presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.esteban.ruano.core.utils.UiText
import com.esteban.ruano.core_ui.R
import com.esteban.ruano.lifecommander.ui.components.Error
import com.esteban.ruano.lifecommander.ui.components.Loading
import com.esteban.ruano.core_ui.utils.CustomSnackbarVisualsWithUiText
import com.esteban.ruano.core_ui.utils.SnackbarController
import com.esteban.ruano.core_ui.utils.SnackbarEvent
import com.esteban.lopez.core.utils.SnackbarType
import com.esteban.ruano.workout_presentation.intent.ExercisesEffect
import com.esteban.ruano.workout_presentation.intent.ExercisesIntent
import com.esteban.ruano.workout_presentation.ui.screens.ExerciseScreen
import com.esteban.ruano.workout_presentation.ui.viewmodel.ExercisesViewModel
import kotlinx.coroutines.launch

@Composable
fun ExercisesDestination(
    workoutId: Int? = null,
    viewModel: ExercisesViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit = {},
    onNewExerciseClick: () -> Unit,
    onExerciseClick: (String?) -> Unit
) {

    val state = viewModel.viewState.collectAsState().value
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current


    /**
     * Handles navigation based on [ExercisesEffect].
     *
     * @param effect The navigation event to handle.
     */
    fun handleNavigation(effect: ExercisesEffect) {
        when (effect) {
            is ExercisesEffect.NavigateUp -> onNavigateUp()
            is ExercisesEffect.ShowSnackbarErrorMessage -> {
                coroutineScope.launch {
                    SnackbarController.sendEvent(
                        SnackbarEvent(
                            CustomSnackbarVisualsWithUiText.fromType(
                                SnackbarType.ERROR,
                                message = UiText.StringResource(R.string.error_unknown))
                        )
                    )
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        // Collect and handle navigation effects.
        coroutineScope.launch { viewModel.effect.collect { handleNavigation(it) } }

        coroutineScope.launch {
           workoutId?.let {
               viewModel.performAction(
                   ExercisesIntent.FetchExercisesByWorkoutDay(it)
               )
           } ?: viewModel.performAction(
               ExercisesIntent.FetchExercises
           )
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
            ExerciseScreen(
                userIntent = {
                    viewModel.performAction(it)
                },
                state = state,
                workoutDayId = workoutId,
                onNewExerciseClick = {
                    onNewExerciseClick()
                },
                onClose = {
                    onNavigateUp()
                },
                onExerciseClick = onExerciseClick
            )
        }
    }


}