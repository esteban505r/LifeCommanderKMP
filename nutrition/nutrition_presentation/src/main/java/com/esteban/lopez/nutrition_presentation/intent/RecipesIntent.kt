package com.esteban.ruano.nutrition_presentation.intent;

import com.esteban.ruano.core_ui.utils.SnackbarType
import com.esteban.ruano.core_ui.view_model.Effect
import com.esteban.ruano.core_ui.view_model.UserIntent

sealed class RecipesIntent : UserIntent {
    data object GetRecipes : RecipesIntent()
    data class GetRecipesByDay(val day: Int) : RecipesIntent()
}

sealed class RecipesEffect : Effect {
    data class ShowSnackBar(val message: String, val type: SnackbarType) : RecipesEffect()
}