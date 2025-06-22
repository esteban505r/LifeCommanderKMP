package com.esteban.ruano.nutrition_data.remote.model

import com.esteban.lopez.nutrition_data.remote.model.IngredientResponse
import com.esteban.lopez.nutrition_data.remote.model.InstructionResponse


data class RecipeResponse(
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
    val days: List<Int> = emptyList(),
    val mealTag:String? = null,
    val ingredients: List<IngredientResponse> = emptyList(),
    val instructions: List<InstructionResponse> = emptyList()
)