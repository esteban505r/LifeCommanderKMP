package com.esteban.ruano.models.nutrition

import com.esteban.ruano.lifecommander.models.AlternativeNutrients
import kotlinx.serialization.Serializable

@Serializable
data class CreateRecipeTrackDTO(
    val recipeId: String,
    val consumedDateTime: String,
    val skipped: Boolean = false,
    val alternativeRecipeId: String? = null,
    val alternativeMealName: String? = null,
    val alternativeNutrients: AlternativeNutrients? = null
) 