package com.esteban.ruano.lifecommander.models

import kotlinx.serialization.Serializable

@Serializable
data class Recipe(
    val id: String,
    val name: String,
    val note: String? = null,
    val protein: Double? = 0.0,
    val calories: Double? = 0.0,
    val carbs: Double? = 0.0,
    val fat: Double? = 0.0,
    val fiber: Double? = 0.0,
    val sugar: Double? = 0.0,
    val image: String? = null,
    val days: List<Int>? = emptyList(),
    val mealTag: String? = null,
    val consumed: Boolean = false,
    val consumedDateTime: String? = null,
    val ingredients: List<Ingredient> = emptyList(),
    val instructions: List<Instruction> = emptyList()
)


@Serializable
data class CreateRecipeTrack(
    val recipeId: String,
    val consumedDateTime: String,
    val skipped: Boolean = false,
    val alternativeRecipeId: String? = null,
    val alternativeMealName: String? = null,
    val alternativeNutrients: AlternativeNutrients? = null
)