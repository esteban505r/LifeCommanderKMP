package com.esteban.ruano.models.nutrition

import kotlinx.serialization.Serializable
import com.esteban.ruano.models.reminders.CreateReminderDTO

@Serializable
data class UpdateRecipeDTO(
    val name: String,
    val note: String? = null,
    val protein: Double? = null,
    val image: String? = null,
    val day: Int? = null,
    val updatedAt: String? = null,
    val mealTag: String? = null,
)