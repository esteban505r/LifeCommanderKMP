package com.esteban.ruano.habits_presentation.navigation

import NutritionDestination
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import androidx.hilt.navigation.compose.hiltViewModel
import com.esteban.ruano.core.routes.Routes
import com.esteban.ruano.nutrition_presentation.ui.screens.navigation.nutritionGraph
import com.esteban.ruano.nutrition_presentation.ui.viewmodel.NutritionViewModel
import com.esteban.ruano.workout_presentation.navigation.WorkoutDestination
import com.esteban.ruano.workout_presentation.navigation.workoutGraph
import com.esteban.ruano.workout_presentation.ui.viewmodel.WorkoutViewModel
import kotlinx.coroutines.launch

// Graph
fun NavGraphBuilder.healthGraph(
    navController: NavHostController,
) {
    navigation(
        route = Routes.BASE.HEALTH.name,
        startDestination = "${Routes.BASE.HEALTH.name}?tab={tab}"
    ) {
        composable(
            route = "${Routes.BASE.HEALTH.name}?tab={tab}",
            arguments = listOf(navArgument("tab") {
                type = NavType.StringType
                defaultValue = "workout"
            })
        ) { entry ->
            val initial = entry.arguments?.getString("tab") ?: "workout"
            HealthTabsScreen(
                navController = navController,
                initialTab = if (initial.equals("nutrition", true)) HealthTab.Nutrition else HealthTab.Workout
            )
        }

        workoutGraph(navController)

        nutritionGraph(navController)
    }
}

enum class HealthTab { Workout, Nutrition }

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun HealthTabsScreen(
    navController: NavHostController,
    initialTab: HealthTab = HealthTab.Workout,
) {
    val initialPage = if (initialTab == HealthTab.Workout) 0 else 1
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { 2 })
    val scope = rememberCoroutineScope()

    Column {
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            backgroundColor = MaterialTheme.colors.background,
            contentColor = MaterialTheme.colors.primary,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                    color = MaterialTheme.colors.primary,
                    height = 3.dp
                )
            }
        ) {
            Tab(
                selected = pagerState.currentPage == 0,
                onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                text = { Text("Workout") }
            )
            Tab(
                selected = pagerState.currentPage == 1,
                onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                text = { Text("Nutrition") }
            )
        }

        HorizontalPager(state = pagerState) { page ->
            when (page) {
                0 -> {
                    val workoutViewModel = hiltViewModel<WorkoutViewModel>()
                    WorkoutDestination(
                        viewModel = workoutViewModel,
                        navController = navController
                    )
                }
                1 -> {
                    val nutritionViewModel = hiltViewModel<NutritionViewModel>()
                    NutritionDestination(
                        viewModel = nutritionViewModel,
                        onNavigateUp = {
                            navController.navigateUp()
                        },
                        onRecipesClick = {
                            navController.navigate(Routes.RECIPES)
                        },
                        onRecipeClick = {
                            navController.navigate("${Routes.RECIPE_DETAIL}/$it")
                        },
                        onConsumeRecipe = {
                            // TODO: Implement
                        },
                        onEditRecipe = {
                            // TODO: Implement
                        },
                        onSkipRecipe = {
                            // TODO: Implement
                        },
                        onDeleteRecipe = {
                            // TODO: Implement
                        }
                    )
                }
            }
        }
    }
}

