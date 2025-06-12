package com.esteban.ruano.models.tasks

import com.esteban.ruano.models.blog.PostCategoryDTO
import kotlinx.serialization.Serializable

@Serializable
data class PostDTO(
    val id: String,
    val title: String,
    val imageUrl: String?,
    val description: String?,
    val tags: List<String>,
    val category: PostCategoryDTO,
    val slug: String,
    val publishedDate: String?,
    val requiresPassword: Boolean
)
