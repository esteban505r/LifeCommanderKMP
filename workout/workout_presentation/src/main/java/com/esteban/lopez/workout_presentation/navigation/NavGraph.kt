package com.esteban.ruano.workout_presentation.navigation

import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.esteban.ruano.core.routes.Routes
import com.esteban.ruano.workout_presentation.ui.screens.AddExerciseToDayScreen
import com.esteban.ruano.workout_presentation.ui.screens.ExerciseScreen
import com.esteban.ruano.workout_presentation.ui.screens.WorkoutDayDetailScreen
import com.esteban.ruano.workout_presentation.ui.screens.WorkoutDayProgressScreen
import com.esteban.ruano.workout_presentation.ui.screens.WorkoutScreen
import com.esteban.ruano.workout_presentation.ui.viewmodel.WorkoutDetailViewModel

fun NavGraphBuilder.workoutGraph(
    navController: NavController,
) {

    composable(Routes.BASE.WORKOUT.name) {
        WorkoutDestination(
            navController = navController
        )
    }

    composable("${Routes.WORKOUT_DAY_DETAIL}/{workoutDayId}") {
        val parentEntry = remember(it) { navController.getBackStackEntry("${Routes.WORKOUT_DAY_DETAIL}/{workoutDayId}") }
        val workoutDetailViewModel:WorkoutDetailViewModel = hiltViewModel(parentEntry)
        val workoutDayId = it.arguments?.getString("workoutDayId")
        WorkoutDayDetailDestination(workoutDayId, navController, viewModel = workoutDetailViewModel)
    }

    composable("${Routes.WORKOUT_DAY_EXERCISES}/{workoutDayId}") {
        val workoutDayId = it.arguments?.getString("workoutDayId")!!.toInt()
        ExercisesDestination(
            workoutId = workoutDayId,
           onNavigateUp = {
               navController.navigateUp()
           },
          onNewExerciseClick = {
            navController.navigate(Routes.NEW_EXERCISE)
          },
            onExerciseClick = {
                navController.navigate("${Routes.EXERCISE_DETAIL}/$it")
            }
        )
    }

    composable(Routes.EXERCISES){
        ExercisesDestination(
            onNavigateUp = {
                navController.navigateUp()
            },
            onNewExerciseClick = {
                navController.navigate(Routes.NEW_EXERCISE)
            },
            onExerciseClick = {
                navController.navigate("${Routes.EXERCISE_DETAIL}/$it")
            }
        )
    }

    composable("${Routes.EXERCISE_DETAIL}/{exerciseId}") {
        ExerciseDetailDestination(
            exerciseId = it.arguments?.getString("exerciseId"),
            onNavigateUp = {
                navController.navigateUp()
            }
        )
    }

    composable(Routes.NEW_EXERCISE){
        NewExerciseDestination(
            navController = navController
        )
    }

    composable("${Routes.WORKOUT_PROGRESS}/{workoutDayId}") {
        val parentEntry = remember(it) { navController.getBackStackEntry("${Routes.WORKOUT_DAY_DETAIL}/{workoutDayId}") }
        val workoutDetailViewModel:WorkoutDetailViewModel = hiltViewModel(parentEntry)
        val workoutDayId = it.arguments?.getString("workoutDayId")
        WorkoutDayProgressDestination(workoutDayId, navController, viewModel = workoutDetailViewModel)
    }

    composable("${Routes.ADD_EXERCISES_TO_WORKOUT_DAY}/{workoutDayId}") {
        val workoutDayId = it.arguments?.getString("workoutDayId")
        AddExercisesToDayDestination (
            workoutDayId = workoutDayId,
            navController = navController
        )
    }
}