package com.esteban.ruano.models.blog

import kotlinx.serialization.Serializable

@Serializable
data class PostResponse(
    val id: String,
    val title: String,
    val slug: String,
    val imageUrl: String?,
    val description: String?,
    val tags: List<String>,
    val category: String,
    val publishedDate: String?,
)
