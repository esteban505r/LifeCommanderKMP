package com.esteban.ruano.lifecommander.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.esteban.ruano.lifecommander.ui.screens.RecipesScreen
import com.esteban.ruano.lifecommander.ui.viewmodels.RecipesViewModel
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

    LaunchedEffect(Unit){
        recipesViewModel.getRecipesByDay(1)
    }

    RecipesScreen(
        onNewRecipe = onNewRecipe,
        onDetailRecipe = onDetailRecipe,
        state = state,
        onGetRecipesByDay = {
            recipesViewModel.getRecipesByDay(it)
        }
    )
} 