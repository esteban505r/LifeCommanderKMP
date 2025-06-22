package com.esteban.ruano.lifecommander.models

import kotlinx.serialization.Serializable

@Serializable
data class Ingredient(
    val id: String? = null,
    val name: String,
    val quantity: Double,
    val unit: String
) 