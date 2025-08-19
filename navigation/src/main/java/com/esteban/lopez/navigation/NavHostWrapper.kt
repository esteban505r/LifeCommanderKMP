package com.esteban.ruano.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.esteban.ruano.core.routes.Routes
import com.esteban.ruano.finance_presentation.ui.navigation.financeGraph
import com.esteban.ruano.habits_presentation.navigation.habitsGraph
import com.esteban.ruano.home_presentation.navigation.homeGraph
import com.esteban.ruano.tasks_presentation.navigation.tasksGraph
import com.esteban.ruano.nutrition_presentation.ui.screens.navigation.nutritionGraph
import com.esteban.ruano.workout_presentation.navigation.workoutGraph

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

        habitsGraph(navController)

        tasksGraph(navController)

        workoutGraph(navController)

        nutritionGraph(navController)

        financeGraph(navController)
    }
}