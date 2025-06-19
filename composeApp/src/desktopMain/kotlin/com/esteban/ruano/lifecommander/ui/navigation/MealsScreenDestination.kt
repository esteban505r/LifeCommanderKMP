package com.esteban.ruano.lifecommander.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.esteban.ruano.lifecommander.ui.components.ErrorScreen
import com.esteban.ruano.lifecommander.ui.components.LoadingScreen
import com.esteban.ruano.lifecommander.ui.screens.RecipesScreen
import com.esteban.ruano.lifecommander.ui.viewmodels.RecipesViewModel
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.plus
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MealsScreenDestination(
    modifier: Modifier = Modifier,
    recipesViewModel: RecipesViewModel = koinViewModel(),
    onNavigateUp: () -> Unit,
    onNewRecipe: () -> Unit,
    onDetailRecipe: (String) -> Unit
) {
    val state by recipesViewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        val now = kotlinx.datetime.Clock.System.now()
        val currentDay = now.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).date.dayOfWeek.value
        recipesViewModel.getRecipesByDay(currentDay)
        
        // Load recipe tracks for the current week
        val startOfWeek = now.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).date.minus(
            kotlinx.datetime.DatePeriod(days = currentDay - 1)
        )
        val endOfWeek = startOfWeek.plus(kotlinx.datetime.DatePeriod(days = 6))
        recipesViewModel.getRecipeTracksByDateRange(
            startDate = startOfWeek.toString(),
            endDate = endOfWeek.toString()
        )
    }

    when {
        state.isLoading -> {
            LoadingScreen(
                message = "Loading recipes...",
                modifier = modifier
            )
        }
        state.isError -> {
            ErrorScreen(
                message = state.errorMessage ?: "Failed to load recipes",
                onRetry = {
                    val now = kotlinx.datetime.Clock.System.now()
                    val currentDay = now.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).date.dayOfWeek.value
                    recipesViewModel.getRecipesByDay(currentDay)
                },
                modifier = modifier
            )
        }
        else -> {
            RecipesScreen(
                state = state,
                onNewRecipe = onNewRecipe,
                onDetailRecipe = onDetailRecipe,
                onGetRecipesByDay = { day ->
                    val now = kotlinx.datetime.Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).date
                    val todayIndex = now.dayOfWeek.value
                    if (day < todayIndex) {
                        recipesViewModel.getConsumedMealsForDay(day)
                    } else {
                        recipesViewModel.getRecipesByDay(day)
                    }
                },
                onGetAllRecipes = {
                    recipesViewModel.getAllRecipes()
                },
                onEditRecipe = { recipe ->
                    recipesViewModel.updateRecipe(recipe)
                },
                onDeleteRecipe = { recipeId ->
                    recipesViewModel.deleteRecipe(recipeId)
                },
                onConsumeRecipe = { recipeId ->
                    recipesViewModel.consumeRecipe(recipeId)
                },
                onSkipRecipe = { recipeId ->
                    recipesViewModel.skipRecipe(recipeId)
                },
                onSkipRecipeWithAlternative = { recipeId, alternativeRecipeId, alternativeMealName ->
                    recipesViewModel.skipRecipeWithAlternative(recipeId, alternativeRecipeId, alternativeMealName)
                }
            )
        }
    }
} 