package com.esteban.ruano.models.blog

import kotlinx.serialization.Serializable

@Serializable
data class PasswordVerificationRequest(
    val password: String
)

@Serializable
data class PasswordVerificationResponse(
    val success: Boolean,
    val message: String? = null
)

@Serializable
data class CreatePostDTO(
    val title: String,
    val slug: String,
    val imageUrl: String? = null,
    val description: String? = null,
    val tags: List<String> = emptyList(),
    val categoryName: String,
    val password: String? = null,
    val publishedDate: String,
    val content: String // The markdown content
)

@Serializable
data class UpdatePostDTO(
    val title: String? = null,
    val slug: String? = null,
    val imageUrl: String? = null,
    val description: String? = null,
    val tags: List<String>? = null,
    val categoryName: String? = null,
    val password: String? = null, // null = no change, empty string = remove password
    val publishedDate: String? = null,
    val content: String? = null
) 