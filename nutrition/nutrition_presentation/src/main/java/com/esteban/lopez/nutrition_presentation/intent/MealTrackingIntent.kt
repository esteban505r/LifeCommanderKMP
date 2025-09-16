package com.esteban.ruano.nutrition_presentation.intent

import com.esteban.lopez.core.utils.SnackbarType
import com.esteban.ruano.core_ui.view_model.Effect
import com.esteban.ruano.core_ui.view_model.UserIntent

sealed class MealTrackingIntent : UserIntent {
    data object RefreshMeals : MealTrackingIntent()
    data class TrackMealConsumed(val recipeId: String) : MealTrackingIntent()
    data class TrackMealSkipped(val recipeId: String) : MealTrackingIntent()
    data class TrackMealSkippedWithAlternative(
        val recipeId: String,
        val alternativeRecipeId: String? = null,
        val alternativeMealName: String? = null
    ) : MealTrackingIntent()
}

sealed class MealTrackingEffect : Effect {
    data class ShowSnackBar(val message: String, val type: SnackbarType) : MealTrackingEffect()
} 