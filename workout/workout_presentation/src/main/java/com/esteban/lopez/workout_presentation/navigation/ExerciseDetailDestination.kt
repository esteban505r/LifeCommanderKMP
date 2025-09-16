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
import com.esteban.ruano.workout_presentation.intent.ExerciseDetailEffect
import com.esteban.ruano.workout_presentation.intent.ExerciseDetailIntent
import com.esteban.ruano.workout_presentation.intent.ExercisesEffect
import com.esteban.ruano.workout_presentation.ui.screens.ExerciseDetailScreen
import com.esteban.ruano.workout_presentation.ui.viewmodel.ExerciseDetailViewModel
import kotlinx.coroutines.launch

@Composable
fun ExerciseDetailDestination(
    exerciseId: String? = null,
    viewModel: ExerciseDetailViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit = {},
) {

    val state = viewModel.viewState.collectAsState().value
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current


    /**
     * Handles navigation based on [ExercisesEffect].
     *
     * @param effect The navigation event to handle.
     */
    fun handleNavigation(effect: ExerciseDetailEffect) {
        when (effect) {
            is ExerciseDetailEffect.NavigateUp -> onNavigateUp()
            is ExerciseDetailEffect.ShowSnackbarErrorMessage -> {
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
           exerciseId?.let {
               viewModel.performAction(
                   ExerciseDetailIntent.FetchExercise(
                       it
                   )
               )
           }
            if (exerciseId == null) {
                SnackbarController.sendEvent(
                    SnackbarEvent(
                        CustomSnackbarVisualsWithUiText.fromType(
                            SnackbarType.ERROR,
                            message = UiText.StringResource(R.string.error_unknown))
                    )
                )
                onNavigateUp()
            }
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
            ExerciseDetailScreen(
                userIntent = {
                    viewModel.performAction(it)
                },
                state = state,
            )
        }
    }


}