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
import com.esteban.ruano.nutrition_presentation.intent.NewEditRecipeEffect
import com.esteban.ruano.nutrition_presentation.intent.NewEditRecipeIntent
import com.esteban.ruano.nutrition_presentation.intent.RecipesEffect
import com.esteban.ruano.nutrition_presentation.ui.screens.NewEditRecipeScreen
import com.esteban.ruano.nutrition_presentation.ui.viewmodel.NewEditRecipeViewModel
import kotlinx.coroutines.launch

@Composable
fun NewRecipeScreenDestination(
    recipeToEditId: String? = null,
    onClose: (Boolean) -> Unit,
    viewModel: NewEditRecipeViewModel = hiltViewModel()
) {
    val editing = recipeToEditId != null
    val state = viewModel.viewState.collectAsState().value
    val coroutineScope = rememberCoroutineScope()

    /**
     * Handles navigation based on [RecipesEffect].
     *
     * @param effect The navigation event to handle.
     */
    fun handleNavigation(effect: NewEditRecipeEffect) {
        when (effect) {
            is NewEditRecipeEffect.CloseScreen -> onClose(effect.wasUpdated)
            else -> {}
        }
    }

    LaunchedEffect(Unit) {
        // Collect and handle navigation effects.
        coroutineScope.launch { viewModel.effect.collect { handleNavigation(it) } }

        if (editing) {
            viewModel.performAction(NewEditRecipeIntent.GetRecipe(recipeToEditId!!))
        }
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
            NewEditRecipeScreen(
                recipeToEdit = state.recipe,
                onClose = onClose,
                userIntent = { viewModel.performAction(it) }
            )
        }
        }
}