package com.esteban.ruano.models.blog

import kotlinx.serialization.Serializable

@Serializable
data class PostCategoryDTO(
    val id: String,
    val name: String,
    val slug: String,
    val description: String?,
    val color: String?,
    val icon: String?,
    val displayOrder: Int,
    val requiresPassword: Boolean,
    val createdDate: String,
    val updatedDate: String
)

@Serializable
data class CreatePostCategoryDTO(
    val name: String,
    val slug: String,
    val description: String? = null,
    val color: String? = null,
    val icon: String? = null,
    val displayOrder: Int = 0,
    val isActive: Boolean = true,
    val password: String? = null
)

@Serializable
data class UpdatePostCategoryDTO(
    val name: String? = null,
    val slug: String? = null,
    val description: String? = null,
    val color: String? = null,
    val icon: String? = null,
    val displayOrder: Int? = null,
    val isActive: Boolean? = null,
    val password: String? = null
) 