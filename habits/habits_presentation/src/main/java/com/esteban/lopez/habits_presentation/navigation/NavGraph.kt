package com.esteban.ruano.habits_presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.esteban.ruano.core.routes.Routes

fun NavGraphBuilder.habitsGraph(
    navController: NavHostController,
) {
    //Habits

    composable(Routes.HABITS.name) {
        HabitsDestination(
            navController = navController
        )
    }

    composable("${Routes.HABIT_DETAIL}/{habitId}") {
        val habitId = it.arguments?.getString("habitId")!!
        HabitDetailDestination (
            onNavigateUp = {
                navController.navigateUp()
            }, onEditClick = { id ->
                navController.navigate("${Routes.NEW_EDIT_HABIT}/$id")
            }, habitId = habitId
        )
    }
    composable(Routes.NEW_EDIT_HABIT) {
        NewHabitDestination(
            onNavigateUp = {
                navController.navigateUp()
            }
        )
    }
    composable("${Routes.NEW_EDIT_HABIT}/{habitId}") {
        NewHabitDestination(
            habitToEditId = it.arguments?.getString("habitId"),
            onNavigateUp = {
                navController.navigateUp()
            }
        )
    }
}