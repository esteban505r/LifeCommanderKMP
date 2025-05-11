package com.esteban.ruano.models.blog

import kotlinx.serialization.Serializable

@Serializable
data class CreatePostRequest(
    val title: String,
    val content: String,
    val publishedDate: String
)
