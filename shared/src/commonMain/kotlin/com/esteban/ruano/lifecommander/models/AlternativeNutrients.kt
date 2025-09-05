package com.esteban.ruano.lifecommander.models

import kotlinx.serialization.Serializable

@Serializable
data class AlternativeNutrients(
    val calories: Double? = null,
    val protein: Double? = null,
    val carbs: Double? = null,
    val fat: Double? = null,
    val fiber: Double? = null,
    val sodium: Double? = null,
    val sugar: Double? = null,
)