package com.esteban.ruano.nutrition_presentation.ui.screens.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.esteban.ruano.core_ui.R
import com.esteban.ruano.lifecommander.ui.components.Error
import com.esteban.ruano.lifecommander.ui.components.Loading
import com.esteban.ruano.nutrition_presentation.intent.RecipeDetailEffect
import com.esteban.ruano.nutrition_presentation.intent.RecipeDetailIntent
import com.esteban.ruano.nutrition_presentation.intent.RecipesEffect
import com.esteban.ruano.nutrition_presentation.ui.screens.RecipeDetailScreen
import com.esteban.ruano.nutrition_presentation.ui.viewmodel.RecipeDetailViewModel
import kotlinx.coroutines.launch

@Composable
fun RecipeDetailDestination(
    id: String,
    onClose: (Boolean) -> Unit,
    onEditRecipe: (String) -> Unit,
    viewModel: RecipeDetailViewModel = hiltViewModel()
) {
    val state = viewModel.viewState.collectAsState().value
    val coroutineScope = rememberCoroutineScope()

    /**
     * Handles navigation based on [RecipesEffect].
     *
     * @param effect The navigation event to handle.
     */
    fun handleNavigation(effect: RecipeDetailEffect) {
        when (effect) {
            is RecipeDetailEffect.CloseScreen -> onClose(effect.wasUpdated)
            is RecipeDetailEffect.EditRecipe -> onEditRecipe(effect.id)
            else -> {}
        }
    }

    LaunchedEffect(Unit) {
        // Collect and handle navigation effects.
        coroutineScope.launch { viewModel.effect.collect { handleNavigation(it) } }

        viewModel.performAction(RecipeDetailIntent.GetRecipe(id))
    }


    when {
        state.isError -> {
            Error(
                message = stringResource(R.string.error_unknown),
            )
        }

        state.isLoading -> {
            Loading()
        }

        else -> {
            RecipeDetailScreen(
                state = state,
                onClose = onClose,
                userIntent = { viewModel.performAction(it) }
            )
        }
        }
}