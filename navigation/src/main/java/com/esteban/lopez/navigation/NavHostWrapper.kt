package com.esteban.ruano.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.esteban.lopez.journal_presentation.navigation.journalGraph
import com.esteban.ruano.core.routes.Routes
import com.esteban.ruano.finance_presentation.ui.navigation.financeGraph
import com.esteban.ruano.habits_presentation.navigation.habitsGraph
import com.esteban.ruano.habits_presentation.navigation.toDoGraph
import com.esteban.ruano.habits_presentation.navigation.healthGraph
import com.esteban.ruano.habits_presentation.navigation.timersGraph
import com.esteban.ruano.home_presentation.navigation.homeGraph
import com.esteban.ruano.tasks_presentation.navigation.tasksGraph
import com.esteban.ruano.nutrition_presentation.ui.screens.navigation.nutritionGraph
import com.esteban.ruano.workout_presentation.navigation.workoutGraph
import com.esteban.lopez.navigation.screens.CalendarScreen
import com.esteban.lopez.navigation.screens.OthersScreen

@Composable
fun NavHostWrapper(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    shouldShowOnboarding: Boolean,
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = Routes.BASE.HOME.name
    ) {

        homeGraph(navController)

        // New Calendar screen (mobile) after Home
        composable(Routes.BASE.CALENDAR.name) {
            CalendarScreen(
                onTaskClick = { taskId ->
                    // Navigate to tasks screen with task filter, or detail if implemented
                    navController.navigate(Routes.TASKS.name)
                },
                onHabitClick = { habitId ->
                    navController.navigate(Routes.HABITS.name)
                }
            )
        }

        toDoGraph(navController)

        healthGraph(navController)

        // Others entry point - shows grid for Finance, Timers, Journal, Study
        composable(Routes.BASE.OTHERS.name) {
            OthersScreen(navController = navController)
        }

        // Keep individual graphs for internal navigation
        timersGraph(navController)
        financeGraph(navController)
        journalGraph(navController)
    }
}