package com.esteban.ruano.nutrition_presentation.intent;

import com.esteban.lopez.core.utils.SnackbarType
import com.esteban.ruano.core_ui.view_model.Effect
import com.esteban.ruano.core_ui.view_model.UserIntent
import com.esteban.ruano.lifecommander.models.Recipe

sealed class NewEditRecipeIntent : UserIntent {
    data class CreateRecipe(
        val name: String,
        val note: String,
        val protein: Double,
        val days: List<Int>,
        val mealTag: String
    ) : NewEditRecipeIntent()

    data class UpdateRecipe(
        val id: String,
        val recipe: Recipe
    ) : NewEditRecipeIntent()
    class GetRecipe(val id: String) : NewEditRecipeIntent()
}

sealed class NewEditRecipeEffect : Effect {
    data class ShowSnackBar(val message: String, val type: SnackbarType) : NewEditRecipeEffect()
    data class CloseScreen(
        val wasUpdated: Boolean = false
    ) : NewEditRecipeEffect()
}