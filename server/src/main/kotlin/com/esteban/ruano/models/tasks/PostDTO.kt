package com.esteban.ruano.models.tasks

import kotlinx.serialization.Serializable

@Serializable
data class PostDTO(
    val id: String,
    val title: String,
    val slug: String,
    val publishedDate: String?,
)
