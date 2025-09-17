package com.esteban.ruano.nutrition_presentation.intent;

import com.esteban.lopez.core.utils.SnackbarType
import com.esteban.ruano.core_ui.view_model.Effect
import com.esteban.ruano.core_ui.view_model.UserIntent
import com.esteban.ruano.lifecommander.models.AlternativeNutrients

sealed class NutritionIntent : UserIntent {
    data class GetDashboard(
        val day: Int
    ) : NutritionIntent()

    data class SearchRecipe(val name:String) : NutritionIntent()
    data class UndoConsumedRecipe(val id:String) : NutritionIntent()
    data class ConsumeRecipe(val recipeId: String, val dateTime: String) : NutritionIntent()

    data class SkipRecipe(
        val recipeId: String, val dateTime: String,
        val alternativeRecipeId: String? = null,
        val alternativeMealName: String? = null,
        val alternativeNutrients: AlternativeNutrients? = null
    ) : NutritionIntent()

    data class DeleteRecipe(val recipeId: String) : NutritionIntent()
}

sealed class NutritionEffect : Effect {
    data class ShowSnackBar(val message: String, val type: SnackbarType) : NutritionEffect()
}