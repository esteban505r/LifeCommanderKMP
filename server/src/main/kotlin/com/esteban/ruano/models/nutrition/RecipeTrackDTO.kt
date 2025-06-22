package com.esteban.ruano.models.nutrition

import kotlinx.serialization.Serializable

@Serializable
data class RecipeTrackDTO(
    val id: String,
    val recipe: RecipeDTO,
    val skipped: Boolean,
    val consumedDateTime: String? = null
) 