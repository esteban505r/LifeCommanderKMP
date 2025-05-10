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
import com.esteban.ruano.core_ui.utils.LocalMainIntent
import com.esteban.ruano.workout_presentation.intent.WorkoutEffect
import com.esteban.ruano.workout_presentation.intent.WorkoutIntent
import com.esteban.ruano.workout_presentation.ui.screens.WorkoutScreen
import com.esteban.ruano.workout_presentation.ui.viewmodel.WorkoutViewModel
import kotlinx.coroutines.launch

@Composable
fun WorkoutDestination(
    viewModel: WorkoutViewModel = hiltViewModel(),
    navController: NavController,
) {

    val state = viewModel.viewState.collectAsState().value
    val sendMainIntent = LocalMainIntent.current
    val coroutineScope = rememberCoroutineScope()


    /**
     * Handles navigation based on [WorkoutEffect].
     *
     * @param effect The navigation event to handle.
     */
    fun handleNavigation(effect: WorkoutEffect) {
        when (effect) {
            else -> {}
        }
    }

    LaunchedEffect(Unit) {
        // Collect and handle navigation effects.
        coroutineScope.launch { viewModel.effect.collect { handleNavigation(it) } }

        coroutineScope.launch {
            viewModel.performAction(
                WorkoutIntent.FetchDashboard
            )
        }
    }

    when {
        state.isError -> {
            Error(
                message = stringResource(R.string.error_unknown),
            )
        }

        state.isLoading -> {
            Loading()
        }


        else -> {
            WorkoutScreen(
                onNavigateUp = { navController.popBackStack() },
                userIntent = {
                    viewModel.performAction(it)
                },
                state = state,
                onWorkoutClick = {
                    navController.navigate("${Routes.WORKOUT_DAY_DETAIL}/$it")
                },
                onExercisesClick = {
                    navController.navigate(Routes.EXERCISES)
                }
            )
        }
    }


}