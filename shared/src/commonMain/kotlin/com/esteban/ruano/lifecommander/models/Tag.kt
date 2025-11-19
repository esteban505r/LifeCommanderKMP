package com.lifecommander.models

import kotlinx.serialization.Serializable

@Serializable
data class Tag(
    val id: String,
    val name: String,
    val slug: String,
    val color: String? = null
)

@Serializable
data class CreateTagRequest(
    val name: String,
    val color: String? = null
)

@Serializable
data class UpdateTagRequest(
    val name: String? = null,
    val color: String? = null
)

@Serializable
data class UpdateTaskTagsRequest(
    val tagIds: List<String>
)

