package com.esteban.ruano.nutrition_presentation.ui.screens.navigation

import NutritionDestination
import RecipesDestination
import android.util.Log
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.esteban.ruano.core.routes.Routes
import com.esteban.ruano.nutrition_presentation.intent.RecipesIntent
import com.esteban.ruano.nutrition_presentation.ui.screens.NutritionScreen
import com.esteban.ruano.nutrition_presentation.ui.viewmodel.RecipesViewModel

fun NavGraphBuilder.nutritionGraph(
    navController: NavController
) {
    composable(Routes.BASE.NUTRITION.name) {
        NutritionDestination(
            onNavigateUp = {
                navController.navigateUp()
            },
            onRecipesClick = {
                navController.navigate(Routes.RECIPES)
            },
            onRecipeClick = {
                navController.navigate("${Routes.RECIPE_DETAIL}/$it")
            }
        )
    }

    composable(Routes.RECIPES) {
        val viewModel = hiltViewModel<RecipesViewModel>()
        RecipesDestination(
            viewModel = viewModel,
            onNavigateUp = {
                navController.navigateUp()
            },
            onNewRecipe = {
                navController.navigate(Routes.NEW_EDIT_RECIPE)
            },
            onDetailRecipe = {
                Log.d("RecipesScreen", "onDetailRecipe: $it")
                navController.navigate("${Routes.RECIPE_DETAIL}/$it")
            }
        )
    }

    composable(Routes.NEW_EDIT_RECIPE) {
        NewRecipeScreenDestination(onClose = {
            navController.navigateUp()
        })
    }
    composable("${Routes.NEW_EDIT_RECIPE}/{recipeId}") {
        NewRecipeScreenDestination(onClose = {
            navController.navigateUp()
        }, recipeToEditId = it.arguments?.getString("recipeId")!!)
    }

    composable("${Routes.RECIPE_DETAIL}/{recipeId}") {
        RecipeDetailDestination(
            id = it.arguments?.getString("recipeId")!!,
            onClose = {
                navController.navigateUp()
            },
            onEditRecipe = {
                navController.navigate("${Routes.NEW_EDIT_RECIPE}/$it")
            }
        )
    }
}