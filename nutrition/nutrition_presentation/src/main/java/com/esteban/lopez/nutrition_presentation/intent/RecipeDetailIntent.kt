package com.esteban.ruano.nutrition_presentation.intent;

import com.esteban.ruano.core_ui.utils.SnackbarType
import com.esteban.ruano.core_ui.view_model.Effect
import com.esteban.ruano.core_ui.view_model.UserIntent
import com.esteban.ruano.nutrition_domain.model.Recipe

sealed class RecipeDetailIntent : UserIntent {
    data class GetRecipe(val id: String) : RecipeDetailIntent()
    data object DeleteRecipe : RecipeDetailIntent()
    data object EditRecipe : RecipeDetailIntent()
}

sealed class RecipeDetailEffect : Effect {
    data class EditRecipe(val id: String) : RecipeDetailEffect()
    data class ShowSnackBar(val message: String, val type: SnackbarType) : RecipeDetailEffect()
    data class CloseScreen(
        val wasUpdated: Boolean = false
    ) : RecipeDetailEffect()
}