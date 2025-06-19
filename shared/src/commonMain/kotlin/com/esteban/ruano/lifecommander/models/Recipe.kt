package com.esteban.ruano.lifecommander.models

import kotlinx.serialization.Serializable

@Serializable
data class Recipe(
    val id: String,
    val name: String,
    val note: String? = null,
    val protein: Double? = null,
    val image: String? = null,
    val day: Int? = null,
    val mealTag: String? = null,
    val consumed: Boolean = false,
    val consumedDateTime: String? = null
)

@Serializable
data class RecipeTrack(
    val id: String,
    val recipeId: String,
    val consumedDateTime: String,
    val status: String = "ACTIVE"
)

@Serializable
data class CreateRecipeTrack(
    val recipeId: String,
    val consumedDateTime: String,
    val skipped: Boolean = false,
    val alternativeRecipeId: String? = null,
    val alternativeMealName: String? = null
)