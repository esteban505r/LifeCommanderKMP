package com.esteban.ruano.models.nutrition

import kotlinx.serialization.Serializable
import com.esteban.ruano.models.reminders.ReminderDTO

@Serializable
data class RecipeDTO(
    val id: String,
    val name: String,
    val note: String? = null,
    val protein: Double? = null,
    val image: String? = null,
    val day: Int? = null,
    val mealTag: String? = null,
)