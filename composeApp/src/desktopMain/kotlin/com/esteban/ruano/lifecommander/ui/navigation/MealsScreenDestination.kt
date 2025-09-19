package com.esteban.ruano.lifecommander.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.esteban.ruano.lifecommander.models.Recipe
import com.esteban.ruano.lifecommander.models.nutrition.RecipeFilters
import com.esteban.ruano.lifecommander.ui.components.ErrorScreen
import com.esteban.ruano.lifecommander.ui.components.LoadingScreen
import com.esteban.ruano.lifecommander.ui.screens.RecipesScreen
import com.esteban.ruano.lifecommander.ui.viewmodels.RecipesViewModel
import com.esteban.ruano.utils.DateUIUtils.getCurrentDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MealsScreenDestination(
    modifier: Modifier = Modifier,
    recipesViewModel: RecipesViewModel = koinViewModel(),
    onNavigateUp: () -> Unit,
    onNewRecipe: (Recipe) -> Unit,
    onDetailRecipe: (String) -> Unit
) {
    val state by recipesViewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        val todayIndex = getCurrentDateTime(TimeZone.currentSystemDefault()).date.dayOfWeek.ordinal + 1
        recipesViewModel.getRecipesByDay(todayIndex)
    }

    when {
        state.isError -> {
            ErrorScreen(
                message = state.errorMessage,
                onRetry = {
                    val todayIndex = getCurrentDateTime(TimeZone.currentSystemDefault()).date.dayOfWeek.ordinal + 1
                    recipesViewModel.getRecipesByDay(todayIndex)
                }
            )
        }
        else -> {
            RecipesScreen(
                state = state,
                onGetRecipesByDay = { day ->
                    recipesViewModel.getRecipesByDay(day)
                },
                onGetAllRecipes = {
                    recipesViewModel.getAllRecipes()
                },
                onGetHistoryForDate = { date ->
                    recipesViewModel.getHistoryForDate(date)
                },
                onNewRecipe = { recipe ->
                    recipesViewModel.addRecipe(recipe)
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
                onSkipRecipeWithAlternative = { recipeId, alternativeRecipeId, alternativeMealName, nutrients ->
                    recipesViewModel.skipRecipeWithAlternative(recipeId, alternativeRecipeId, alternativeMealName, nutrients)
                },
                onSearchRecipes = { query ->
                    recipesViewModel.searchRecipes(query)
                },
                onClearSearch = {
                    recipesViewModel.clearSearch()
                },
                onApplyFilters = { filters ->
                    recipesViewModel.getRecipesWithFilters(filters = filters)
                },
                onClearAllFilters = {
                    recipesViewModel.getRecipesWithFilters(filters = RecipeFilters())
                }
            )
        }
    }
} 