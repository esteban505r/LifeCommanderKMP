package com.esteban.ruano.workout_presentation.navigation

import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.esteban.ruano.core_ui.R
import com.esteban.ruano.lifecommander.ui.components.Error
import com.esteban.ruano.lifecommander.ui.components.Loading
import com.esteban.ruano.workout_presentation.intent.WorkoutIntent
import com.esteban.ruano.workout_presentation.ui.screens.WorkoutDayProgressScreen
import com.esteban.ruano.workout_presentation.ui.viewmodel.WorkoutDetailViewModel
import com.esteban.ruano.workout_presentation.ui.viewmodel.state.WorkoutDayDetailEffect
import kotlinx.coroutines.launch

@Composable
fun WorkoutDayProgressDestination(
    workoutId: String? = null,
    navController: NavController,
    viewModel: WorkoutDetailViewModel = hiltViewModel(),
) {

    val state = viewModel.viewState.collectAsState().value
    val coroutineScope = rememberCoroutineScope()

    var pagerState by remember {
        mutableStateOf(
            PagerState(pageCount = { state.exercisesInProgress.size })
        )
    }

    LaunchedEffect(state.exercisesInProgress) {
        pagerState = PagerState(pageCount = { state.exercisesInProgress.size })
    }

    /**
     * Handles navigation based on [WorkoutDayDetailEffect].
     *
     * @param effect The navigation event to handle.
     */
    fun handleNavigation(effect: WorkoutDayDetailEffect) {
        when (effect) {
            is WorkoutDayDetailEffect.NavigateUp -> navController.navigateUp()
            WorkoutDayDetailEffect.AnimateToNextExercise -> {
                coroutineScope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
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
                    WorkoutIntent.FetchWorkoutByDay(it)
                )
            } ?: navController.navigateUp()
        }
        coroutineScope.launch {
            viewModel.performAction(WorkoutIntent.StartTimer)
        }
    }

    when {
        state.errorMessage != null -> {
            Error(
                message = stringResource(R.string.error_unknown),
            )
        }

        state.isLoading -> {
            Loading()
        }


        else -> {
            WorkoutDayProgressScreen(
                pagerState = pagerState,
                onClose = {
                    navController.navigateUp()
                },
                state = state,
                userIntent = {
                    viewModel.performAction(it)
                }
            )
        }
    }


}