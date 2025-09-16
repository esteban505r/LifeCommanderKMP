package com.esteban.ruano.nutrition_presentation.intent;

import com.esteban.lopez.core.utils.SnackbarType
import com.esteban.ruano.core_ui.view_model.Effect
import com.esteban.ruano.core_ui.view_model.UserIntent

sealed class NutritionIntent : UserIntent {
    data class GetDashboard(
        val date: String
    ) : NutritionIntent()
}

sealed class NutritionEffect : Effect {
    data class ShowSnackBar(val message: String, val type: SnackbarType) : NutritionEffect()
}