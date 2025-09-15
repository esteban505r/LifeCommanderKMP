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
import com.esteban.ruano.core_ui.utils.CustomSnackbarVisualsWithUiText
import com.esteban.ruano.core_ui.utils.SnackbarController
import com.esteban.ruano.core_ui.utils.SnackbarEvent
import com.esteban.ruano.core_ui.utils.SnackbarType
import com.esteban.ruano.workout_presentation.intent.ExercisesEffect
import com.esteban.ruano.workout_presentation.ui.screens.NewEditExerciseScreen
import com.esteban.ruano.workout_presentation.ui.viewmodel.ExercisesViewModel
import kotlinx.coroutines.launch

@Composable
fun NewExerciseDestination(
    viewModel: ExercisesViewModel = hiltViewModel(),
    navController: NavController,
    exerciseToEditId: String? = null
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

    LaunchedEffect(Unit) {
        coroutineScope.launch { viewModel.effect.collect { handleNavigation(it) } }
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
            NewEditExerciseScreen (
                userIntent = {
                    viewModel.performAction(it)
                },
                exerciseToEditId = exerciseToEditId,
                state = state
            )
        }
    }


}