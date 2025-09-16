package com.esteban.ruano.nutrition_presentation.intent;

import com.esteban.lopez.core.utils.SnackbarType
import com.esteban.ruano.core_ui.view_model.Effect
import com.esteban.ruano.core_ui.view_model.UserIntent
import com.esteban.ruano.lifecommander.models.nutrition.RecipeFilters

sealed class RecipesIntent : UserIntent {
    data object GetRecipes : RecipesIntent()
    data class GetRecipesByDay(val day: Int) : RecipesIntent()
    data object GetAllRecipes : RecipesIntent()
    data class GetHistoryForDate(val date: String) : RecipesIntent()
    data class SearchRecipes(val query: String) : RecipesIntent()
    data object ClearSearch : RecipesIntent()
    data class ApplyFilters(val filters: RecipeFilters) : RecipesIntent()
    data object ClearAllFilters : RecipesIntent()
    data class SetViewMode(val viewMode: com.esteban.ruano.nutrition_presentation.ui.viewmodel.state.ViewMode) : RecipesIntent()
}

sealed class RecipesEffect : Effect {
    data class ShowSnackBar(val message: String, val type: SnackbarType) : RecipesEffect()
}