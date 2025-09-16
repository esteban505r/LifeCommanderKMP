package com.esteban.ruano.workout_presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.esteban.ruano.core.routes.Routes
import com.esteban.ruano.core_ui.R
import com.esteban.ruano.lifecommander.ui.components.Error
import com.esteban.ruano.lifecommander.ui.components.Loading
import com.esteban.ruano.workout_presentation.intent.WorkoutIntent
import com.esteban.ruano.workout_presentation.ui.screens.WorkoutDayDetailScreen
import com.esteban.ruano.workout_presentation.ui.viewmodel.WorkoutDetailViewModel
import com.esteban.ruano.workout_presentation.ui.viewmodel.state.WorkoutDayDetailEffect
import kotlinx.coroutines.launch

@Composable
fun WorkoutDayDetailDestination(
    day: String? = null,
    navController: NavController,
    viewModel: WorkoutDetailViewModel = hiltViewModel(),
    ) {

    val state = viewModel.viewState.collectAsState().value
    val coroutineScope = rememberCoroutineScope()

    val lifecycleOwner = navController.currentBackStackEntryAsState().value?.lifecycle

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                coroutineScope.launch {
                    day?.let {
                        viewModel.performAction(
                            WorkoutIntent.FetchWorkoutByDay(it)
                        )
                    } ?: navController.navigateUp()
                }
            }
        }
        lifecycleOwner?.addObserver(observer)
        onDispose { lifecycleOwner?.removeObserver(observer) }
    }

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
                dayId = day?: "",
                onClose = {
                    navController.navigateUp()
                },
                onStartWorkout = {
                    navController.navigate("${Routes.WORKOUT_PROGRESS}/$day")
                },
                onEditExercise = {
                    navController.navigate("${Routes.NEW_EDIT_EXERCISE}/$it")
                },
                onDeleteExercise = {
                    viewModel.performAction(WorkoutIntent.DeleteExercise(it){
                        coroutineScope.launch {
                            day?.let {
                                viewModel.performAction(
                                    WorkoutIntent.FetchWorkoutByDay(it)
                                )
                            } ?: navController.navigateUp()
                        }
                    })
                },
                onAddExercisesClick = {
                    navController.navigate("${Routes.ADD_EXERCISES_TO_WORKOUT_DAY}/$day")
                },
                onUnlinkFromDay = { exerciseId ->
                    day?.let {
                        viewModel.performAction(WorkoutIntent.UnlinkExerciseFromDay(exerciseId, it) {
                            coroutineScope.launch {
                                viewModel.performAction(
                                    WorkoutIntent.FetchWorkoutByDay(it)
                                )
                            }
                        })
                    }
                }
            )
        }
    }


}