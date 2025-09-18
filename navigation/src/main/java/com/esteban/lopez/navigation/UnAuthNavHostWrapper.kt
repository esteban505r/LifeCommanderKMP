package com.esteban.ruano.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.esteban.lopez.onboarding_presentation.auth.navigation.ForgotPasswordDestination
import com.esteban.lopez.onboarding_presentation.auth.navigation.LoginDestination
import com.esteban.lopez.onboarding_presentation.auth.navigation.SignUpDestination
import com.esteban.ruano.core.routes.Routes
import com.esteban.ruano.finance_presentation.ui.navigation.financeGraph
import com.esteban.ruano.habits_presentation.navigation.habitsGraph
import com.esteban.ruano.habits_presentation.navigation.toDoGraph
import com.esteban.ruano.home_presentation.navigation.homeGraph
import com.esteban.ruano.tasks_presentation.navigation.tasksGraph
import com.esteban.ruano.nutrition_presentation.ui.screens.navigation.nutritionGraph
import com.esteban.ruano.workout_presentation.navigation.workoutGraph

@Composable
fun UnAuthNavHostWrapper(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = Routes.Unauthenticated.LOGIN
    ) {

        composable(
            route = Routes.Unauthenticated.LOGIN,
        ) {
            LoginDestination(
                onForgotPassword = {
                    navController.navigate(Routes.Unauthenticated.FORGOT_PASSWORD)
                },
                onSignUp = {
                    navController.navigate(Routes.Unauthenticated.SIGN_UP)
                }
            )
        }

        composable(
            route = Routes.Unauthenticated.SIGN_UP
        ){
            SignUpDestination (
                onBack = { navController.navigateUp() }
            )
        }

        composable(
            route = Routes.Unauthenticated.FORGOT_PASSWORD,
        ){
            ForgotPasswordDestination(
                onBack = { navController.navigateUp() }
            )
        }
    }
}