package com.esteban.ruano.models.tags

import kotlinx.serialization.Serializable

@Serializable
data class TagDTO(
    val id: String,
    val name: String,
    val slug: String,
    val color: String? = null
)

@Serializable
data class CreateTagDTO(
    val name: String,
    val color: String? = null
)

@Serializable
data class UpdateTagDTO(
    val name: String? = null,
    val color: String? = null
)

@Serializable
data class UpdateTaskTagsDTO(
    val tagIds: List<String>
)

