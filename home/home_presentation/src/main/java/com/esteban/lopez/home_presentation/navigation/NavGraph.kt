package com.esteban.ruano.home_presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.esteban.ruano.core.routes.Routes
import com.esteban.ruano.home_presentation.HomeScreen

fun NavGraphBuilder.homeGraph(
    navController: NavController
){
    composable(Routes.BASE.HOME.name) {
        HomeScreen(
            onGoToTasks = {
                navController.navigate(Routes.BASE.TASKS.name)
            },
            onGoToWorkout = {
                navController.navigate(Routes.BASE.WORKOUT.name)
            },
            onCurrentHabitClick = {
                navController.navigate(Routes.BASE.HABITS.name)
            }
        )
    }
}