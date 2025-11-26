package com.esteban.ruano.habits_presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.esteban.ruano.core.routes.Routes
import com.esteban.ruano.timers_presentation.navigation.TimersDestination

fun NavGraphBuilder.timersGraph(
    navController: NavHostController,
) {
    composable(Routes.BASE.TIMERS.name) {
        TimersDestination(navController = navController)
    }

    composable("${Routes.TIMER_LIST_DETAIL}/{listId}") { backStackEntry ->
        val listId = backStackEntry.arguments?.getString("listId") ?: ""
        // TODO: Implement TimerListDetailDestination
        androidx.compose.material.Text("Timer List Detail: $listId")
    }
}

