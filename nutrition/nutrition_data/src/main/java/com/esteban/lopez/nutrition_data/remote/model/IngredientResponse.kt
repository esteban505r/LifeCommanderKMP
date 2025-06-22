package com.esteban.lopez.nutrition_data.remote.model


data class IngredientResponse(
    val id: String?,
    val name: String,
    val quantity: Double,
    val unit: String
) 