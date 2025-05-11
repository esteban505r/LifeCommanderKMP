package com.esteban.ruano.models.resource

import kotlinx.serialization.Serializable

@Serializable
data class ResourceDTO(
    val id: Int,
    val url: String,
    val type: String
)