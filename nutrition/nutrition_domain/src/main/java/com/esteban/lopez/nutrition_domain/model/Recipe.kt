package com.esteban.ruano.nutrition_domain.model

data class Recipe(
    val id: String,
    val name: String,
    val note: String? = null,
    val protein: Double? = null,
    val image: String? = null,
    val day: Int? = null,
    val mealTag:String? = null
)