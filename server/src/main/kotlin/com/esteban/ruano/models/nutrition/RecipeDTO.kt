package com.esteban.ruano.models.nutrition

import kotlinx.serialization.Serializable
import com.esteban.ruano.models.reminders.ReminderDTO

@Serializable
data class RecipeDTO(
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
    val ingredients: List<IngredientDTO> = emptyList(),
    val instructions: List<InstructionDTO> = emptyList()
)