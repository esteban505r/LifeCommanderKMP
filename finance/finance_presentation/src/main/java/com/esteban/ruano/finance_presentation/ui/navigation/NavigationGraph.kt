package com.esteban.ruano.finance_presentation.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.esteban.ruano.core.routes.Routes
import com.esteban.ruano.finance_presentation.navigation.FinanceDestination

fun NavGraphBuilder.financeGraph(
    navController: NavController
) {
    composable(Routes.BASE.FINANCE.name) {
        FinanceDestination(
            navController = navController,
        )
    }


}