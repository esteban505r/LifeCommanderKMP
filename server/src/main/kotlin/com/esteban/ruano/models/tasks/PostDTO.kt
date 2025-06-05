package com.esteban.ruano.models.tasks

import kotlinx.serialization.Serializable

@Serializable
data class PostDTO(
    val id: String,
    val title: String,
    val imageUrl: String?,
    val description: String?,
    val tags: List<String>,
    val category: String,
    val slug: String,
    val publishedDate: String?,
)
