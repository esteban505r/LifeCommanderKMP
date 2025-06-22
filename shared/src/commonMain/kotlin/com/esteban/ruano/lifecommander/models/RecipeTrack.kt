package com.esteban.ruano.lifecommander.models

import kotlinx.serialization.Serializable

@Serializable
data class RecipeTrack(
    val id: String,
    val recipe: Recipe,
    val skipped: Boolean,
    val consumedDateTime: String? = null
) 